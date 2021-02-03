package org.folio.rest.delegate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseQueryDelegate extends AbstractDelegate {

  private Expression name;
  private Expression query;

  @Autowired
  private DatabaseConnectionService connectionService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    String nameString = name.getValue(execution).toString();
    String queryString = query.getValue(execution).toString();

    String identifier = (String) execution.getVariable(nameString);

    Connection conn = connectionService.getConnection(identifier);

    try (Statement statement = conn.createStatement()) {
      statement.execute(queryString);

      ResultSet results = null;
      if (statement.getUpdateCount() == -1) {
        results = statement.getResultSet();
      }
    }


    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

}
