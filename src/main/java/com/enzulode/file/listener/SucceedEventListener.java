package com.enzulode.file.listener;

import com.enzulode.file.event.FileSucceedEvent;
import com.enzulode.file.service.FileEventListeningOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SucceedEventListener {

  private final FileEventListeningOperationsService operationsService;

  @RabbitListener(queues = "${spring.rabbitmq.mapping.file-events.succeed-file.queue}")
  public void handleSucceedFileEvent(FileSucceedEvent event) {
    log.info("Processing succeed file event!");
    operationsService.fileSucceedEvent(event.objectName(), event.onBehalfOf());
  }
}
