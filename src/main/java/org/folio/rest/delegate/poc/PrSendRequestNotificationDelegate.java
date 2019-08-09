package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class PrSendRequestNotificationDelegate extends TestAbstractRuntimeDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Send Request Notification Delegate");

    String orderId = execution.getVariable("orderId").toString();
    String bookTitle = execution.getVariable("bookTitle").toString();
    String bookId = execution.getVariable("bookId").toString();

    log.info("Order {} has arrived, book {} with id {} has been received", orderId, bookTitle, bookId);
  }

}
