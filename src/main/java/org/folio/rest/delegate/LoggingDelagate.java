package org.folio.rest.delegate;

import com.fasterxml.jackson.databind.JsonNode;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class LoggingDelagate extends AbstractRuntimeDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    System.out.println("\n\n ITS HERE!");
    JsonNode payloadNode = (JsonNode) execution.getVariable("payload");
    System.out.println(payloadNode);
  }

}
