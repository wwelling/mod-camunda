package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class CrTimerNotifyDelegate extends TestAbstractLoggingDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Claim Returned Timer Notification Delegate");

    String itemId = execution.getVariable("itemId").toString();
    String count = execution.getVariable("checkedCount").toString();

    log.info("5 minute timing interval has passed. The count for item {} is {}", itemId, count);
  }

}
