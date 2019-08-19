package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class CrExternalCheckInDelegate extends TestAbstractLoggingDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing External Claim Returned Notification Delegate");

    String itemId = execution.getVariable("itemId").toString();

    log.info("Item {} checked in from external source, removing item from open Claims Returned list.", itemId);
  }

}
