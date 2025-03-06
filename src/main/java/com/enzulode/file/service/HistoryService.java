package com.enzulode.file.service;

import com.enzulode.file.dto.HistoryItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoryService {

  Page<HistoryItemDto> getHistory(Pageable pageable);
}
