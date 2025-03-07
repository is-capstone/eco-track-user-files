package com.enzulode.file.api;

import com.enzulode.file.dto.FinishFastUploadDto;
import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.dto.StartFastUploadResponseDto;
import com.enzulode.file.facade.UploadApiFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

  private final UploadApiFacade facade;

  @GetMapping("/start")
  public StartFastUploadResponseDto start(
      @RequestParam("metricsId") Long metricsId,
      @RequestParam("filename") String filename,
      @RequestParam(name = "type", required = false) String type
  ) {
    return facade.start(metricsId, filename, type);
  }

  @PostMapping("/part")
  public PartDetailsDto uploadPart(
      @RequestParam("uploadId") String uploadId,
      @RequestParam("partNumber") int partNumber,
      @RequestParam("fileId") UUID fileId,
      @RequestPart("part") MultipartFile part
  ) {
    return facade.uploadPart(uploadId, partNumber, fileId, part);
  }

  @PostMapping("/finish")
  public void finish(@RequestBody FinishFastUploadDto finishFastUploadDto) {
    facade.finish(finishFastUploadDto);
  }
}
