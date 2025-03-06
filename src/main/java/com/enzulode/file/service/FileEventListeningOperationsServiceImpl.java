package com.enzulode.file.service;

import com.enzulode.file.dao.repository.FileMetadataRepository;
import com.enzulode.file.integration.s3.S3OperationsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileEventListeningOperationsServiceImpl implements FileEventListeningOperationsService {

  private static final String TMP_FOLDER_NAME = "tmp";

  private final FileMetadataRepository metadataRepository;
  private final S3OperationsClient client;

  @Override
  @Transactional
  public void fileFailedEvent(String objName, String onBehalfOf) {
    metadataRepository.markFailedByNameAndOwnedBy(objName, onBehalfOf);
    client.deleteObject(TMP_FOLDER_NAME, FileUtils.getFilename(objName));
  }

  @Override
  @Transactional
  public void fileSucceedEvent(String objName, String onBehalfOf) {
    metadataRepository.markSucceedByNameAndOwnedBy(objName, onBehalfOf);
    client.moveObject(TMP_FOLDER_NAME, onBehalfOf, FileUtils.getFilename(objName));
  }
}
