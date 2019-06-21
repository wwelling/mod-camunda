package org.folio.rest.jms;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.jms.model.Event;
import org.folio.rest.tenant.storage.ThreadLocalStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import static org.camunda.spin.Spin.JSON;

@Component
public class EventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

  private static final String CHECK_OUT_PATH = "/events/circulation/check-out-by-barcode";

  @Value("${event.queue.name}")
  private String eventQueueName;

  @Autowired
  protected RuntimeService runtimeService;

  @JmsListener(destination = "${event.queue.name}")
  public void receive(Event event) {
    logger.info("Receive [{}]: {}, {}, {}, {}", eventQueueName, event.getMethod(), event.getPath(), event.getTriggerType(), event.getPayload());
    logger.info("Event: {}", event.getPathPattern(), event.getTriggerId());

    SpinJsonNode jsonNode = JSON(event.getPayload());
    if (jsonNode.hasProp("errors")) {
      logger.info("Event contains error in payload");
      return;
    }

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
    logger.info("Starting correlate message");

    String[] eventPathArr = event.getPath().split("/");

    if (event.getPathPattern().equals("/circulation/loans/{id}")) {
      String tenant = event.getTenant();
      String businessKey = eventPathArr[4];

      // Correlate message
      MessageCorrelationResult result = runtimeService.createMessageCorrelation("MessageClaimReturnedExternal")
        .tenantId(tenant)
        .processInstanceBusinessKey(businessKey)
        .correlateWithResult();
        logger.info("Message Result: {}, Process Instance Id: {}",
        result,
        result.getExecution().getProcessInstanceId());
    } else {
      
      String tenant = event.getTenant();
      JsonNode payloadNode = event.getPayload();



      // Correlate message
      ProcessInstance processInstance = runtimeService.createMessageCorrelation(event.getPathPattern())
        .tenantId(tenant)
        .setVariable("payload", payloadNode)
        .correlateStartMessage();
     
      logger.info("New Process Instance Id: {}", processInstance.getProcessInstanceId());

    } 
  }

  private void startProcess(Event event) {
    if (!event.getProcessDefinitionIds().isEmpty()) {
      event.getProcessDefinitionIds().forEach(processDefinitionId -> {
        runtimeService.startProcessInstanceById(processDefinitionId);
      });
    } else {
      if (event.getPath().equals(CHECK_OUT_PATH)) {
        logger.info("Starting Claims Returned");

        String tenant = event.getTenant();

        SpinJsonNode jsonNode = JSON(event.getPayload());
        String businessKey = jsonNode.prop("id").stringValue();
        logger.info("JSON NODE: {}", jsonNode);

        Map<String, Object> variables = new HashMap<>();
        variables.put("checkOutJson", jsonNode);
        variables.put("userId", jsonNode.prop("userId").stringValue());
        variables.put("itemId", jsonNode.prop("itemId").stringValue());
        variables.put("status", jsonNode.prop("status").prop("name").stringValue());
        variables.put("checkedCount", 0);

        // Start Claims Returned Process
        ProcessInstance processInstance = runtimeService.createMessageCorrelation("MessageStartClaimReturned")
          .tenantId(tenant)
          .processInstanceBusinessKey(businessKey)
          .setVariables(variables)
          .correlateStartMessage();
        logger.info("New Process Instance Id: {}", processInstance.getProcessInstanceId());
      }
    }
  }

  private void completeTask(Event event) {

  }

}
