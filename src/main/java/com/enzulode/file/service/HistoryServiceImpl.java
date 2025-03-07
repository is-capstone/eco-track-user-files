package com.enzulode.file.service;

import com.enzulode.file.dao.entity.FileMetadataEntity;
import com.enzulode.file.dao.repository.FileMetadataRepository;
import com.enzulode.file.dto.HistoryItemDto;
import com.enzulode.file.util.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

  private final FileMetadataRepository repository;
  private final SecurityContextHelper contextHelper;

  private static final Function<FileMetadataEntity, HistoryItemDto> mapper =
      (el) -> new HistoryItemDto(el.getName(), el.getOwnedBy(), el.getStatus());

  @Override
  public Page<HistoryItemDto> getHistory(Pageable pageable) {
    if (contextHelper.isAdmin()) {
      return repository.findAll(pageable)
          .map(mapper);
    }
    return repository.findByOwnedBy(contextHelper.findUserName(), pageable)
        .map(mapper);
  }
}
