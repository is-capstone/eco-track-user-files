package com.enzulode.file.dto;

import java.util.List;
import java.util.UUID;

public record FinishFastUploadDto(String uploadId, UUID fileId, List<PartDetailsDto> parts) {}
