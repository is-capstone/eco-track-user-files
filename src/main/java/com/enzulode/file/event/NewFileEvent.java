package com.enzulode.file.event;

import java.time.LocalDateTime;

public record NewFileEvent(Long metricsId, String objectName, String owner, LocalDateTime at, SupportedFileType type) {

  public enum SupportedFileType { CSV, JSON }
}
