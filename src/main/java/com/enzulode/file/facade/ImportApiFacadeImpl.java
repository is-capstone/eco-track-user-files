package com.enzulode.file.facade;

import com.enzulode.file.event.NewFileEvent;
import com.enzulode.file.event.NewFileEvent.SupportedFileType;
import com.enzulode.file.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportApiFacadeImpl implements ImportApiFacade {

  private final ImportService importService;

  @Override
  @Async
  public void upload(Long metricsId, String name, InputStream file, SupportedFileType fileType, String owner) {
    importService.upload(metricsId, name, file, fileType, owner);
  }
}
