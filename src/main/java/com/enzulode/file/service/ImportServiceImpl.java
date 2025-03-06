package com.enzulode.file.service;

import com.enzulode.file.dao.entity.FileMetadataEntity;
import com.enzulode.file.dao.entity.FileMetadataEntity.FileStatus;
import com.enzulode.file.dao.repository.FileMetadataRepository;
import com.enzulode.file.event.NewFileEvent;
import com.enzulode.file.event.NewFileEvent.SupportedFileType;
import com.enzulode.file.integration.rabbitmq.RabbitMQProducer;
import com.enzulode.file.integration.s3.S3OperationsClient;
import com.enzulode.file.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportServiceImpl implements ImportService {

  private static final String TMP_FOLDER_NAME = "tmp";

  @Value("${spring.rabbitmq.mapping.file-events.new-file.exchange}") private String exchange;
  @Value("${spring.rabbitmq.mapping.file-events.new-file.routing-key}") private String routingKey;

  private final S3OperationsClient client;
  private final FileMetadataRepository metadataRepository;
  private final RabbitMQProducer producer;

  @Override
  public void upload(Long id, String fileName, InputStream is, SupportedFileType fileType, String owner) {
    var fullObjName = "%s%s".formatted(TMP_FOLDER_NAME + "/", fileName);
    var future = client.save(TMP_FOLDER_NAME, fileName, is);
    future.whenCompleteAsync((cu, ex) -> {
      if (ex != null) {
        log.error(ex.getMessage(), ex);
        return;
      }

      // save metadata
      var fileMetadata = new FileMetadataEntity(fullObjName, owner, FileStatus.PROCESSING);
      metadataRepository.save(fileMetadata);

      // notify
      var newFileEvent = new NewFileEvent(id, fullObjName, owner, LocalDateTime.now(), fileType);
      producer.send(exchange, routingKey, newFileEvent);
    });
  }

  @Override
  public InputStream get(String folderName, String objectName) {
    return client.get(folderName, objectName);
  }
}
