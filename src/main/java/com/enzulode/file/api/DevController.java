package com.enzulode.file.api;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.text.MessageFormat;
import java.util.List;

@RestController
@PreAuthorize("hasRole('DEV')")
@RequestMapping("/dev/fast-upload")
@RequiredArgsConstructor
public class DevController {

  public record ErrorDto(List<String> messages) {}

  public record StartFastUploadResponseDto(String uploadId) {}
  public record PartDetailsDto(int partNumber, String eTag) {}
  public record FinishFastUploadDto(String uploadId, String objectKey, List<PartDetailsDto> parts) {}

  private static final String BUCKET = "import-metrics";

  private final S3Client client;

  @GetMapping("/start")
  public StartFastUploadResponseDto start(
      @RequestParam("key") String key,
      @RequestParam(name = "type", required = false) String type
  ) {
    var contentType = "JSON".equalsIgnoreCase(type) ? "application/json" : "text/csv";
    var createMPartUploadReq = CreateMultipartUploadRequest.builder()
        .bucket(BUCKET)
        .key(key)
        .contentType(contentType)
        .build();

    var response = client.createMultipartUpload(createMPartUploadReq);
    AwsSdkUtil.checkSdkResponse(response);
    return new StartFastUploadResponseDto(response.uploadId());
  }

  @PostMapping("/part")
  public PartDetailsDto uploadPart(
      @RequestParam("uploadId") String uploadId,
      @RequestParam("partNumber") int partNumber,
      @RequestParam("key") String key,
      @RequestPart("part") MultipartFile part
  ) {
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
      var abortReq = AbortMultipartUploadRequest.builder()
          .uploadId(uploadId)
          .bucket(BUCKET)
          .key(key)
          .build();
      var response = client.abortMultipartUpload(abortReq);
      AwsSdkUtil.checkSdkResponse(response);
      throw new RuntimeException("MULTIPART UPLOAD ABORTION", e);
    }
  }

  @PostMapping("/finish")
  public void finish(@org.springframework.web.bind.annotation.RequestBody FinishFastUploadDto finishUploadDto) {
    var parts = finishUploadDto.parts()
        .stream().map(part -> CompletedPart.builder()
            .eTag(part.eTag())
            .partNumber(part.partNumber())
            .build())
        .toList();
    var completedMPartUpload = CompletedMultipartUpload.builder()
        .parts(parts)
        .build();
    var completeMPartUploadReq = CompleteMultipartUploadRequest.builder()
        .uploadId(finishUploadDto.uploadId())
        .bucket(BUCKET)
        .key(finishUploadDto.objectKey())
        .multipartUpload(completedMPartUpload)
        .build();

    try {
      var response = client.completeMultipartUpload(completeMPartUploadReq);
      AwsSdkUtil.checkSdkResponse(response);
    } catch (Exception e) {
      var abortReq = AbortMultipartUploadRequest.builder()
          .uploadId(finishUploadDto.uploadId())
          .bucket(BUCKET)
          .key(finishUploadDto.objectKey())
          .build();
      var response = client.abortMultipartUpload(abortReq);
      AwsSdkUtil.checkSdkResponse(response);
      throw new RuntimeException("MULTIPART UPLOAD ABORTION", e);
    }
  }


  public static class AwsSdkUtil {
    public static void checkSdkResponse(SdkResponse sdkResponse) {
      if (AwsSdkUtil.isErrorSdkHttpResponse(sdkResponse)) {
        throw new RuntimeException(MessageFormat.format("{0} - {1}", sdkResponse.sdkHttpResponse().statusCode(), sdkResponse.sdkHttpResponse().statusText()));
      }
    }

    public static boolean isErrorSdkHttpResponse(SdkResponse response) {
      return response.sdkHttpResponse() == null || !response.sdkHttpResponse().isSuccessful();
    }
  }
}
