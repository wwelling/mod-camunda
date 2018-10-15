package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MasterSendStartMessageDelegate implements JavaDelegate {

  private static final Logger log = LoggerFactory.getLogger(MasterSendStartMessageDelegate.class);

  @Autowired
  private RuntimeService runtimeService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Master Send Message");

    String eventPath = execution.getVariable("eventPath").toString();

    Random ran = new Random();
    int bk = ran.nextInt(10000) + 10000;
    String businessKey = "pk" + Integer.toString(bk);

    if (eventPath.equals("sendMessage")) {
      execution.setVariable("process2BusinessKey", businessKey);
      runtimeService
        .createMessageCorrelation("Message_StartProcess2")
        .processInstanceBusinessKey(businessKey)
        .setVariable("masterStartVariable", "masterStartVariableValue")
        .correlateStartMessage();
    }

    if (eventPath.equals("sendTask")) {
      execution.setVariable("process3BusinessKey", businessKey);
      runtimeService
        .createMessageCorrelation("Message_StartProcess3")
        .processInstanceBusinessKey(businessKey)
        .setVariable("masterStartVariable", "masterStartVariableValue")
        .correlateStartMessage();
    }
  }
}
