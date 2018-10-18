package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class CrIncrementCheckCountDelegate extends AbstractLoggingDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Increment Check Count Delegate");

    Long newCount = ((Long) execution.getVariable("checkedCount") + 1);

    execution.setVariable("checkedCount", newCount);
    log.info("New count is {}", newCount.toString());
  }
}
