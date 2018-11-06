package org.folio.rest.jms;

import java.io.IOException;

import org.camunda.bpm.engine.RuntimeService;
import org.folio.rest.jms.model.Event;
import org.folio.rest.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  public void receive(String message) throws JsonParseException, JsonMappingException, IOException {
    logger.info("Receive [{}]: {}", eventQueueName, message);
    Event event = objectMapper.readValue(message, Event.class);

    ThreadLocalStorage.setTenant(event.getTenant());

    switch (event.getTriggerType()) {
    case MESSAGE_CORRELATE:

      break;
    case PROCESS_START:
      event.getProcessDefinitionIds().forEach(processDefinitionId -> {
        runtimeService.startProcessInstanceById(processDefinitionId);
      });
      break;
    case TASK_COMPLETE:

      break;
    default:
      break;
    }

  }

}
