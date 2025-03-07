package com.enzulode.file.service;

import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.dto.StartFastUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MultipartUploadService {

  StartFastUploadResponseDto start(String key, String type, UUID fileId);

  PartDetailsDto uploadPart(String uploadId, int partNumber, String key, MultipartFile part);

  void finish(String uploadId, String key, List<PartDetailsDto> partsData);
}
