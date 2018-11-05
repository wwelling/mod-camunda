package org.folio.rest.jms;

import java.io.IOException;

import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  @Value("${event.queue.name}")
  private String eventQueueName;

  @Autowired
  protected RuntimeService runtimeService;

  @Autowired
  private ObjectMapper mapper;

  @JmsListener(destination = "${event.queue.name}")
  public void receive(String message) throws JsonParseException, JsonMappingException, IOException {
    logger.info("Receive [{}]: {}", eventQueueName, message);

    JsonNode event = mapper.readValue(message, JsonNode.class);

    JsonNode processDefinitionIds = event.get("processDefinitionIds");

    if (processDefinitionIds.isArray()) {
      for (final JsonNode processDefinitionId : processDefinitionIds) {
        logger.info("Starting process: {}", processDefinitionId);
        runtimeService.startProcessInstanceById(processDefinitionId.asText());
      }
    }

  }

}
