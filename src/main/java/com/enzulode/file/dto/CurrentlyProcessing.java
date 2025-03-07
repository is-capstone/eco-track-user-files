package com.enzulode.file.dto;

import com.enzulode.file.event.NewFileEvent.SupportedFileType;

public record CurrentlyProcessing(Long metricsId, String filename, String owner, SupportedFileType fileType, String key) {}
