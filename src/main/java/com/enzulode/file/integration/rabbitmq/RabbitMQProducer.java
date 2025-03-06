package com.enzulode.file.integration.rabbitmq;

public interface RabbitMQProducer {

  void send(String exchange, String routingKey, Object message);

  void sendFanout(String exchange, Object message);
}
