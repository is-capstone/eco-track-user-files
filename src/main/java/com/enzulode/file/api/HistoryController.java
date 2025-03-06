package com.enzulode.file.api;

import com.enzulode.file.dto.HistoryItemDto;
import com.enzulode.file.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
@Slf4j
@RequiredArgsConstructor
public class HistoryController {

  private final HistoryService historyService;

  @GetMapping
  public Page<HistoryItemDto> getHistory(Pageable pageable) {
    return historyService.getHistory(pageable);
  }
}
