package com.enzulode.file.service;

import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.dto.StartFastUploadResponseDto;
import com.enzulode.file.util.AwsSdkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockingMultipartUploadServiceImpl implements MultipartUploadService {

  private static final String BUCKET = "import-metrics";

  private final S3Client client;

  @Override
  public StartFastUploadResponseDto start(String key, String type, UUID fileId) {
    var createMPartUploadReq = CreateMultipartUploadRequest.builder()
        .bucket(BUCKET)
        .key(key)
        .contentType(type)
        .build();

    var response = client.createMultipartUpload(createMPartUploadReq);
    AwsSdkUtil.checkSdkResponse(response);
    return new StartFastUploadResponseDto(response.uploadId(), fileId);
  }

  @Override
  public PartDetailsDto uploadPart(String uploadId, int partNumber, String key, MultipartFile part) {
    var uploadPartReq = UploadPartRequest.builder()
        .uploadId(uploadId)
        .partNumber(partNumber)
        .bucket(BUCKET)
        .key(key)
        .contentLength(part.getSize())
        .build();
    try {
      var reqBody = RequestBody.fromInputStream(part.getInputStream(), part.getSize());
      var response = client.uploadPart(uploadPartReq, reqBody);
      AwsSdkUtil.checkSdkResponse(response);

      return new PartDetailsDto(partNumber, response.eTag());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      var abortReq = AbortMultipartUploadRequest.builder()
          .uploadId(uploadId)
          .bucket(BUCKET)
          .key(key)
          .build();
      var response = client.abortMultipartUpload(abortReq);
      AwsSdkUtil.checkSdkResponse(response);
      throw new RuntimeException("MULTIPART UPLOAD ABORTION", e); // TODO: custom exception
    }
  }

  @Override
  public void finish(String uploadId, String key, List<PartDetailsDto> partsData) {
    var parts = partsData
        .stream().map(part -> CompletedPart.builder()
            .eTag(part.eTag())
            .partNumber(part.partNumber())
            .build())
        .toList();
    var completedMPartUpload = CompletedMultipartUpload.builder()
        .parts(parts)
        .build();
    var completeMPartUploadReq = CompleteMultipartUploadRequest.builder()
        .uploadId(uploadId)
        .bucket(BUCKET)
        .key(key)
        .multipartUpload(completedMPartUpload)
        .build();

    try {
      var response = client.completeMultipartUpload(completeMPartUploadReq);
      AwsSdkUtil.checkSdkResponse(response);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      var abortReq = AbortMultipartUploadRequest.builder()
          .uploadId(uploadId)
          .bucket(BUCKET)
          .key(key)
          .build();
      var response = client.abortMultipartUpload(abortReq);
      AwsSdkUtil.checkSdkResponse(response);
      throw new RuntimeException("MULTIPART UPLOAD ABORTION", e); // TODO: custom exception
    }
  }
}
