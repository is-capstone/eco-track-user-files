package com.enzulode.file.integration.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQProducerImpl implements RabbitMQProducer {

  private final AmqpTemplate amqpTemplate;

  public RabbitMQProducerImpl(@Qualifier("defaultAmqpTemplate") AmqpTemplate amqpTemplate) {
    this.amqpTemplate = amqpTemplate;
  }

  @Override
  public void send(String exchange, String routingKey, Object message) {
    amqpTemplate.convertAndSend(exchange, routingKey, message);
  }

  @Override
  public void sendFanout(String exchange, Object message) {
    amqpTemplate.convertAndSend(exchange,"", message);
  }
}
