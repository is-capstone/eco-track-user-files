package com.enzulode.file.listener;

import com.enzulode.file.event.FileFailedEvent;
import com.enzulode.file.service.FileEventListeningOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FailedEventListener {

  private final FileEventListeningOperationsService operationsService;

  @RabbitListener(queues = "${spring.rabbitmq.mapping.file-events.failed-file.queue}")
  public void handleFailedFileEvent(FileFailedEvent event) {
    log.info("Processing failed file event!");
    operationsService.fileFailedEvent(event.objName(), event.onBehalfOf());
  }
}
