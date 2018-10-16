package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterSendImeDelegate implements JavaDelegate {

  private static final Logger log = LoggerFactory.getLogger(MasterSendImeDelegate.class);
  private static final String RECEIVE_MESSAGE_1 = "Message_ReceiveEvent1";
  private static final String TENANT_ID = "diku";

  @Autowired
  private RuntimeService runtimeService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Master Send Intermediate Message Event");

    runtimeService
      .createMessageCorrelation(RECEIVE_MESSAGE_1)
      .tenantId(TENANT_ID)
      .processInstanceBusinessKey(execution.getVariable("process2BusinessKey").toString())
      .setVariable("masterMessageTaskVariable", "masterMessageTaskVariable")
      .correlate();
  }

}
