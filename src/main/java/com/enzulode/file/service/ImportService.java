package com.enzulode.file.service;

import com.enzulode.file.event.NewFileEvent.SupportedFileType;

import java.io.InputStream;

public interface ImportService {

  void upload(Long id, String fileName, InputStream is, SupportedFileType fileType, String owner);

  InputStream get(String folderName, String objectName);
}
