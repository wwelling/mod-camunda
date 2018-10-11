package org.folio.rest.delegate;

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThrowRuntimeErrorDelegate implements JavaDelegate {

  private final static Logger log = LoggerFactory.getLogger(System1Delegate.class);

  @Override
  public void execute(DelegateExecution execution) {
    try {
      log.info("Throwing runtime error...");

      ArrayList arr = new ArrayList<>();
      arr.get(1);
    } catch (Exception e) {
      String message = e.getMessage();
      throw new BpmnError("RUNTIME_ERROR", message);
    }
  }

}
