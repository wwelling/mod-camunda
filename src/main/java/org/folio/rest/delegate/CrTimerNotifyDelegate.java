package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class CrTimerNotifyDelegate extends AbstractLoggingDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Claim Returned Timer Notification Delegate");

    String book = execution.getVariable("bookId").toString();
    String count = execution.getVariable("checkedCount").toString();

    log.info("5 minute timing interval has passed. The count for book {} is {}", book, count);
  }

}
