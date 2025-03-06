package com.enzulode.file.integration.s3;

import com.enzulode.file.integration.s3.exception.S3OperationClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadRequest;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3OperationsClientImpl implements S3OperationsClient {

  @Value("${spring.aws.s3.bucket}") private String bucket;

  private final S3Client client;
  private final S3TransferManager transferManager;

  @Override
  public boolean objectExists(String folderName, String objectName) {
    try {
      var objectExistsReq = HeadObjectRequest.builder()
          .bucket(bucket)
          .key("%s%s".formatted( folderName.endsWith("/") ? folderName : folderName + "/", objectName ))
          .build();
      client.headObject(objectExistsReq);
      return true;
    } catch (Exception e) {
      log.trace("Object '{}' in folder '{}' does not exist or s3 is inaccessible", objectName, folderName);
      return false;
    }
  }

  @Override
  public CompletableFuture<CompletedUpload> save(String folderName, String objectName, InputStream is) {
    if (objectExists(folderName, objectName)) {
      log.trace("Object '{}' in folder '{}' already exists", objectName, folderName);
      return CompletableFuture.failedFuture(new S3OperationClientException("Object '" + objectName + "' already exists"));
    }

    var objKey = "%s%s".formatted( folderName.endsWith("/") ? folderName : folderName + "/", objectName );

    try {
      var uploadReq = UploadRequest.builder()
          .putObjectRequest(por -> por.bucket(bucket).key(objKey))
          .requestBody(
              AsyncRequestBody.fromInputStream(
                  is, (long) is.available(),
                  Executors.newVirtualThreadPerTaskExecutor()
              )
          )
          .build();
      return transferManager.upload(uploadReq).completionFuture();
//          .thenAccept(ur -> {
//            log.info("Object '{}' in folder '{}' uploaded", objectName, folderName);
//             TODO: insert file metadata in the database
//          });
    } catch (Exception e) {
      log.error("Failed to save object '{}' in folder '{}': s3 is inaccessible", objectName, folderName);
      throw new S3OperationClientException("Failed to save");
    }
  }

  @Override
  public InputStream get(String folderName, String objectName) {
    if (!objectExists(folderName, objectName)) return InputStream.nullInputStream();

    try {
      var getObjReq = GetObjectRequest.builder()
          .bucket(bucket)
          .key("%s%s".formatted( folderName.endsWith("/") ? folderName : folderName + "/", objectName ))
          .build();
      return client.getObject(getObjReq);
    } catch (Exception e) {
      return InputStream.nullInputStream();
    }
  }

  @Override
  public void moveObject(String sourceFolder, String targetFolder, String objectName) {
    if (!objectExists(sourceFolder, objectName)) throw new S3OperationClientException("Source folder or object does not exist");

    var sourceObjKey = "%s%s".formatted( sourceFolder.endsWith("/") ? sourceFolder : sourceFolder + "/", objectName );
    var destObjKey = "%s%s".formatted( targetFolder.endsWith("/") ? targetFolder : targetFolder + "/", objectName );

    try {
      // copy obj
      var copyObjectReq = CopyObjectRequest.builder()
          .sourceBucket(bucket)
          .destinationBucket(bucket)
          .sourceKey(sourceObjKey)
          .destinationKey(destObjKey)
          .build();
      client.copyObject(copyObjectReq);

      // wait on copy
      var waiterCopyReq = HeadObjectRequest.builder()
          .bucket(bucket)
          .key(destObjKey)
          .build();
      client.waiter().waitUntilObjectExists(waiterCopyReq);

      // delete old obj & wait
      deleteObject(sourceFolder, objectName);
    } catch (Exception e) {
      log.debug("Failed to move object '{}' in folder '{}'", objectName, targetFolder);
      throw new S3OperationClientException("Failed to move object", e);
    }
  }

  @Override
  public void deleteObject(String folderName, String objectName) {
    if (!objectExists(folderName, objectName)) return;

    var objKey = "%s%s".formatted( folderName.endsWith("/") ? folderName : folderName + "/", objectName );

    try {
      // delete old obj
      var deleteOldObjReq = DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(objKey)
          .build();
      client.deleteObject(deleteOldObjReq);

      // wait on delete old obj
      var waiterDeleteReq = HeadObjectRequest.builder()
          .bucket(bucket)
          .key(objKey)
          .build();
      client.waiter().waitUntilObjectNotExists(waiterDeleteReq);
    } catch (Exception e) {
      log.warn("Failed to delete object '{}' in folder '{}'", objectName, folderName);
      throw new S3OperationClientException("Failed to delete object", e);
    }
  }
}
