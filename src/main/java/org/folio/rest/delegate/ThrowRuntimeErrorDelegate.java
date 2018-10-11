package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThrowRuntimeErrorDelegate implements JavaDelegate {

  private static final Logger log = LoggerFactory.getLogger(ThrowRuntimeErrorDelegate.class);

  @Override
  public void execute(DelegateExecution execution) {
    try {
      log.info("Throwing runtime error...");
      throw new RuntimeException("This is an example exception!");
    } catch (Exception e) {
      String message = e.getMessage();
      throw new BpmnError("RUNTIME_ERROR", message);
    }
  }

}
