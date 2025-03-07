package com.enzulode.file.facade;

import com.enzulode.file.dto.FinishFastUploadDto;
import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.dto.StartFastUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UploadApiFacade {

  StartFastUploadResponseDto start(Long metricsId, String filename, String type);

  PartDetailsDto uploadPart(String uploadId, int partNumber, UUID fileId, MultipartFile part);

  void finish(FinishFastUploadDto finishFastUploadDto);
}
