package org.folio.rest.jms;

import static org.camunda.spin.Spin.JSON;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.workflow.jms.model.Event;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Value("${event.queue.name}")
  private String eventQueueName;

  @Autowired
  protected RuntimeService runtimeService;

  @JmsListener(destination = "${event.queue.name}")
  public void receive(Event event) {
    logger.info("Receive [{}]: {}, {}, {}", eventQueueName, event.getMethod(), event.getPath(), event.getPayload());
    logger.info("Event: {}", event.getPathPattern(), event.getTriggerId());

    SpinJsonNode jsonNode = JSON(event.getPayload());
    if (jsonNode.hasProp("errors")) {
      logger.info("Event contains error in payload");
      return;
    }

    String tenant = event.getTenant();

    ThreadLocalStorage.setTenant(tenant);

    logger.info("Correlating message {}", event.getPathPattern());

    JsonNode payload = event.getPayload();

    runtimeService.createMessageCorrelation(event.getPathPattern())
      .tenantId(tenant)
      .setVariable("payload", payload)
      .correlateStartMessage();
  }

}
