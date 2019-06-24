package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestAccumulatorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();
    System.out.println(String.format("%s STARTED", delegateName));
    streamService.getFlux().buffer(500).subscribe(rows -> {
      System.out.println(String.format("%s: %s", delegateName, rows.size()));
    });
  }

}
