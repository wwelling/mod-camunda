package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestProcessDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService; 

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();
    streamService.map(d -> {
      System.out.println(String.format("%s: %s\n", delegateName, d));
      return d;
    });
  }

}

