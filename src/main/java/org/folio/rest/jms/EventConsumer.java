package org.folio.rest.jms;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.jms.model.Event;
import org.folio.rest.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static org.camunda.spin.Spin.JSON;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  private static final String CHECK_OUT_PATH = "/events/circulation/check-out-by-barcode";
  private static final String CHECK_IN_PATH = "/events/circulation/loans";

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
    if (event.getProcessDefinitionIds().size() > 0) {
      event.getProcessDefinitionIds().forEach(processDefinitionId -> {
        runtimeService.startProcessInstanceById(processDefinitionId);
      });
    } else {
      if (event.getPath().equals(CHECK_OUT_PATH)) {
        logger.info("Starting Claims Returned");
        // Parse event object for data to start process

        String tenant = event.getTenant();

        SpinJsonNode jsonNode = JSON(event.getPayload());
        String businessKey = jsonNode.prop("id").stringValue();
        logger.info("JSON NODE: {}", jsonNode);

        // Start Claims Returned Process
        //runtimeService.startProcessInstanceByMessage("MessageStartClaimReturned", "businessKey");
        runtimeService.createMessageCorrelation("MessageStartClaimReturned")
          .tenantId(tenant)
          .processInstanceBusinessKey(businessKey)
          .setVariable("checkOutJson", jsonNode)
          .correlateStartMessage();
      }
    }
  }

  private void completeTask(Event event) {

  }

}
