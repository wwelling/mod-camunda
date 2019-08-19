package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service
public class PrNewOrderNotificationDelegate extends TestAbstractRuntimeDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing New Order Notification Delegate");

    String orderId = execution.getVariable("orderId").toString();
    String bookTitle = execution.getVariable("bookTitle").toString();
    String bookId = execution.getVariable("bookId").toString();

    log.info("New order {} has been created for book {} with bookId {}", orderId, bookTitle, bookId);
  }

}
