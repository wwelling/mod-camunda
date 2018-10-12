package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterSendMessageDelegate implements JavaDelegate {

  private static final Logger log = LoggerFactory.getLogger(MasterSendMessageDelegate.class);

  @Autowired
  private RuntimeService runtimeService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Master Send Message");

    runtimeService
      .createMessageCorrelation("Message_StartProcess2")
      .processInstanceBusinessKey("businessKey")
      .setVariable("masterStartVariable", "masterStartVariableValue")
      .correlateStartMessage();
  }

}
