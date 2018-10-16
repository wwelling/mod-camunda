package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class MasterSendStartMessageDelegate implements JavaDelegate {

  private static final Logger log = LoggerFactory.getLogger(MasterSendStartMessageDelegate.class);
  private static final String EVENT_PATH = "eventPath";
  private static final String SEND_MESSAGE = "sendMessage";
  private static final String SEND_TASK = "sendTask";
  private static final String START_MESSAGE_2 = "Message_StartProcess2";
  private static final String START_MESSAGE_3 = "Message_StartProcess3";
  private static final SecureRandom rand = new SecureRandom();

  @Autowired
  private RuntimeService runtimeService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Master Send Message");

    String eventPath = execution.getVariable(EVENT_PATH).toString();

    int bk = rand.nextInt(100000);
    String businessKey = "pk" + Integer.toString(bk);

    if (eventPath.equals(SEND_MESSAGE)) {
      execution.setVariable("process2BusinessKey", businessKey);
      runtimeService
        .createMessageCorrelation(START_MESSAGE_2)
        .processInstanceBusinessKey(businessKey)
        .setVariable("masterStartVariable", "masterStartVariableValue")
        .correlateStartMessage();
    }

    if (eventPath.equals(SEND_TASK)) {
      execution.setVariable("process3BusinessKey", businessKey);
      runtimeService
        .createMessageCorrelation(START_MESSAGE_3)
        .processInstanceBusinessKey(businessKey)
        .setVariable("masterStartVariable", "masterStartVariableValue")
        .correlateStartMessage();
    }
  }
}
