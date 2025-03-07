package com.enzulode.file.service;


import com.enzulode.file.dto.FinishFastUploadDto;
import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.dto.StartFastUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface MultipartUploadService {

  StartFastUploadResponseDto start(String key, String type);

  PartDetailsDto uploadPart(String uploadId, int partNumber, String key, MultipartFile part);

  void finish(FinishFastUploadDto finishFastUploadDto);
}
