package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class CrExternalCheckInDelegate extends AbstractLoggingDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing External Claim Returned Notification Delegate");

    String book = execution.getVariable("bookId").toString();

    log.info("Book {} checked in from external source, terminating process.", book);
  }

}
