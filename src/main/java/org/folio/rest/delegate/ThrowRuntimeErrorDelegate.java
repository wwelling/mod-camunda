package org.folio.rest.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class ThrowRuntimeErrorDelegate implements JavaDelegate {

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
