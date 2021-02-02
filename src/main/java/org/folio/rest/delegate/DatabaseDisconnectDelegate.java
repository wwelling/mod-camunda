package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseDisconnectDelegate extends AbstractDelegate {

  private Expression name;

  @Autowired
  private DatabaseConnectionService connectionService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    String nameString = name.getValue(execution).toString();

    String identifier = (String) execution.getVariable(nameString);

    connectionService.destroyConnection(identifier);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setName(Expression name) {
    this.name = name;
  }

}
