package com.enzulode.file.facade;

import com.enzulode.file.event.NewFileEvent.SupportedFileType;

import java.io.InputStream;

public interface ImportApiFacade {

  void upload(Long metricsId, String name, InputStream file, SupportedFileType fileType, String owner);
}
