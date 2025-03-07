package com.enzulode.file.facade;

import com.enzulode.file.dao.entity.FileMetadataEntity;
import com.enzulode.file.dao.repository.FileMetadataRepository;
import com.enzulode.file.dto.CurrentlyProcessing;
import com.enzulode.file.dto.FinishFastUploadDto;
import com.enzulode.file.dto.PartDetailsDto;
import com.enzulode.file.dto.StartFastUploadResponseDto;
import com.enzulode.file.event.NewFileEvent;
import com.enzulode.file.integration.rabbitmq.RabbitMQProducer;
import com.enzulode.file.service.MultipartUploadService;
import com.enzulode.file.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.enzulode.file.event.NewFileEvent.SupportedFileType.CSV;
import static com.enzulode.file.event.NewFileEvent.SupportedFileType.JSON;

@Service
@Slf4j
@RequiredArgsConstructor
public class InMemoryUploadApiFacadeImpl implements UploadApiFacade {

  private static final String TMP_FOLDER_NAME = "tmp";

  @Value("${spring.rabbitmq.mapping.file-events.new-file.exchange}") private String exchange;
  @Value("${spring.rabbitmq.mapping.file-events.new-file.routing-key}") private String routingKey;

  private final Map<UUID, CurrentlyProcessing> processing = new ConcurrentHashMap<>();

  private final MultipartUploadService uploadService;
  private final RabbitMQProducer producer;
  private final SecurityContextHelper contextHelper;
  private final FileMetadataRepository metadataRepository;

  @Override
  public StartFastUploadResponseDto start(Long metricsId, String filename, String type) {
    var owner = contextHelper.findUserName();
    var fileUuid = UUID.randomUUID();
    var fileType = "JSON".equalsIgnoreCase(type) ? JSON : CSV;
    var key = TMP_FOLDER_NAME + "/%s%s".formatted(fileUuid.toString(), filename);
    processing.put(fileUuid, new CurrentlyProcessing(metricsId, filename, owner, fileType, key));

    var contentType = "JSON".equalsIgnoreCase(type) ? "application/json" : "text/csv";
    return uploadService.start(key, contentType, fileUuid);
  }

  @Override
  public PartDetailsDto uploadPart(String uploadId, int partNumber, UUID fileId, MultipartFile part) {
    var processingNow = processing.get(fileId);
    return uploadService.uploadPart(uploadId, partNumber, processingNow.key(), part);
  }

  @Transactional
  @Override
  public void finish(FinishFastUploadDto finishFastUploadDto) {
    var processed = processing.remove(finishFastUploadDto.fileId());
    uploadService.finish(finishFastUploadDto.uploadId(), processed.key(), finishFastUploadDto.parts());

    var newFileMetadataEntry = new FileMetadataEntity(processed.filename(), processed.owner(), FileMetadataEntity.FileStatus.PROCESSING);
    metadataRepository.save(newFileMetadataEntry);

    var newFileEvent = new NewFileEvent(processed.metricsId(), processed.key(), processed.owner(), LocalDateTime.now(), processed.fileType());
    producer.send(exchange, routingKey, newFileEvent);
  }
}
