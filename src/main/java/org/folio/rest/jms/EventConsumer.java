package org.folio.rest.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.folio.spring.messaging.model.Event;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Value("${event.queue.name}")
  private String eventQueueName;

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private ObjectMapper objectMapper;

  @JmsListener(destination = "${event.queue.name}")
  public void receive(Event event) throws JsonProcessingException {
    logger.info("Receive [{}]: {}, {}, {}", eventQueueName, event.getMethod(), event.getPath(), event.getPayload());
    logger.info("Event: {}", event.getPathPattern(), event.getTriggerId());

    String tenant = event.getTenant();

    ThreadLocalStorage.setTenant(tenant);

    logger.info("Correlating message {}", event.getPathPattern());

    // @formatter:off
    Map<String, Object> variables = objectMapper.convertValue(event.getPayload(),
        new TypeReference<Map<String, Object>>() {});

    runtimeService.createMessageCorrelation(event.getPathPattern())
      .tenantId(tenant)
      .setVariables(variables)
      .correlateStartMessage();
    // @formatter:on
  }

}
