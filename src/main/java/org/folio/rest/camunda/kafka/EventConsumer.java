package org.folio.rest.camunda.kafka;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.folio.spring.messaging.model.Event;
import org.folio.spring.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private ObjectMapper objectMapper;

  @KafkaListener(
    id = "mod-camunda-events-listener",
    containerFactory = "kafkaListenerContainerFactory",
    topicPattern = "${application.kafka.listener.events.topic-pattern}",
    groupId = "${application.kafka.listener.events.group-id}",
    concurrency = "${application.kafka.listener.events.concurrency}"
  )
  public void receive(Event event) throws JsonProcessingException {
    logger.info("Receive: {}, {}, {}", event.getMethod(), event.getPath(), event.getPayload());
    logger.info("Event: {}", event.getPathPattern(), event.getTriggerId());

    String tenant = event.getTenant();

    ThreadLocalStorage.setTenant(tenant);

    logger.info("Correlating message {}", event.getPathPattern());

    Map<String, Object> variables = objectMapper.convertValue(event.getPayload(),
        new TypeReference<Map<String, Object>>() {});

    runtimeService.createMessageCorrelation(event.getPathPattern())
      .tenantId(tenant)
      .setVariables(variables)
      .correlateStartMessage();
  }

}
