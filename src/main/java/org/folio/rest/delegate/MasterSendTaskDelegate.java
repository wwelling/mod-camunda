package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterSendTaskDelegate implements JavaDelegate {

  private static final Logger log = LoggerFactory.getLogger(MasterSendTaskDelegate.class);
  private static final String RECEIVE_MESSAGE_1 = "Message_ReceiveTask1";

  @Autowired
  private RuntimeService runtimeService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Master Send Task");

    runtimeService
      .createMessageCorrelation(RECEIVE_MESSAGE_1)
      .tenantId(execution.getTenantId())
      .processInstanceBusinessKey(execution.getVariable("process3BusinessKey").toString())
      .setVariable("masterMessageTaskVariable", "masterMessageTaskVariable")
      .correlate();
  }

}
