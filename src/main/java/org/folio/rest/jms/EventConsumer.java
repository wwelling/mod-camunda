package org.folio.rest.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Value("${event.queue.name}")
  private String eventQueueName;

  @JmsListener(destination = "${event.queue.name}")
  public void receive(String message) {
    logger.info("Receive [{}]: {}", eventQueueName, message);
  }

}
