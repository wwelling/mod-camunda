package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class CrIncrementCheckCountDelegate extends TestAbstractLoggingDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Increment Check Count Delegate");

    Integer newCount = ((Integer) execution.getVariable("checkedCount") + 1);

    execution.setVariable("checkedCount", newCount);
    log.info("New count is {}", newCount);
  }
}
