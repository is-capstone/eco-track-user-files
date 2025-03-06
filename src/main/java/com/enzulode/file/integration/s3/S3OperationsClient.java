package com.enzulode.file.integration.s3;

import software.amazon.awssdk.transfer.s3.model.CompletedUpload;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface S3OperationsClient {

  boolean objectExists(String folderName, String objectName);

  CompletableFuture<CompletedUpload> save(String folderName, String objectName, InputStream is);

  InputStream get(String folderName, String objectName);

  void moveObject(String sourceFolder, String targetFolder, String objectName);

  void deleteObject(String folderName, String objectName);
}
