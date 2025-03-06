package com.enzulode.file.api;

import com.enzulode.file.event.NewFileEvent;
import com.enzulode.file.facade.ImportApiFacade;
import com.enzulode.file.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

  private final ImportApiFacade importFacade;
  private final SecurityContextHelper contextHelper;

  @PostMapping(value = "/{metricsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public void uploadFile(
      @PathVariable Long metricsId,
      @RequestPart("file") MultipartFile file,
      @RequestParam(name = "type", required = false) String type
  ) {
    var filename = "%s_%s".formatted(UUID.randomUUID().toString(), file.getOriginalFilename());
    if (type == null || type.isBlank()) type = "CSV";
    NewFileEvent.SupportedFileType ft = NewFileEvent.SupportedFileType.valueOf(type.toUpperCase());
    try {
      importFacade.upload(metricsId, filename, file.getInputStream(), ft, contextHelper.findUserName());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
