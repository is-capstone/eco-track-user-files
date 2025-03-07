package com.enzulode.file.api;

import com.enzulode.file.dto.FinishFastUploadDto;
import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.service.MultipartUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/dev/fast-upload")
@RequiredArgsConstructor
public class QuickUploadController {

  private final MultipartUploadService uploadService;

  @GetMapping("/start")
  public com.enzulode.file.dto.StartFastUploadResponseDto start(
      @RequestParam("key") String key,
      @RequestParam(name = "type", required = false) String type
  ) {
    var contentType = "JSON".equalsIgnoreCase(type) ? "application/json" : "text/csv";
    return uploadService.start(key, contentType);
  }

  @PostMapping("/part")
  public PartDetailsDto uploadPart(
      @RequestParam("uploadId") String uploadId,
      @RequestParam("partNumber") int partNumber,
      @RequestParam("key") String key,
      @RequestPart("part") MultipartFile part
  ) {
    return uploadService.uploadPart(uploadId, partNumber, key, part);
  }

  @PostMapping("/finish")
  public void finish(@RequestBody FinishFastUploadDto finishFastUploadDto) {
    uploadService.finish(finishFastUploadDto);
  }
}
