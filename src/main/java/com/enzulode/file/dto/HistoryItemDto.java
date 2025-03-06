package com.enzulode.file.dto;

import com.enzulode.file.dao.entity.FileMetadataEntity.FileStatus;

public record HistoryItemDto(String name, String ownedBy, FileStatus status) {}
