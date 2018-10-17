package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class MasterSendTaskDelegate extends AbstractRuntimeDelegate {

  private static final String RECEIVE_MESSAGE_1 = "Message_ReceiveTask1";

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Master Send Task");
    // @formatter:off
    runtimeService
      .createMessageCorrelation(RECEIVE_MESSAGE_1)
      .tenantId(execution.getTenantId())
      .processInstanceBusinessKey(execution.getVariable("process3BusinessKey").toString())
      .setVariable("masterMessageTaskVariable", "masterMessageTaskVariable")
      .correlate();
    // @formatter:on
  }

}
