package com.enzulode.file.dto;

import java.util.List;

public record FinishFastUploadDto(String uploadId, String objectKey, List<PartDetailsDto> parts) {}
