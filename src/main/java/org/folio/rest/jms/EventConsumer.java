package org.folio.rest.jms;

import org.camunda.bpm.engine.RuntimeService;
import org.folio.rest.jms.model.Event;
import org.folio.rest.tenant.storage.ThreadLocalStorage;
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
  protected RuntimeService runtimeService;

  @JmsListener(destination = "${event.queue.name}")
  public void receive(Event event) {
    logger.info("Receive [{}]: {}, {}, {}", eventQueueName, event.getMethod(), event.getPath(), event.getPayload());

    String tenant = event.getTenant();

    ThreadLocalStorage.setTenant(tenant);

    switch (event.getTriggerType()) {
    case MESSAGE_CORRELATE:
      correlateMessage(event);
      break;
    case PROCESS_START:
      startProcess(event);
      break;
    case TASK_COMPLETE:
      completeTask(event);
      break;
    default:
      break;
    }

  }

  private void correlateMessage(Event event) {

  }

  private void startProcess(Event event) {
    event.getProcessDefinitionIds().forEach(processDefinitionId -> {
      runtimeService.startProcessInstanceById(processDefinitionId);
    });
  }

  private void completeTask(Event event) {

  }

}
