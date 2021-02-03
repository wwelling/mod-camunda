package org.folio.rest.delegate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.DatabaseQueryTask;

public class DatabaseQueryDelegate extends AbstractDatabaseOutputDelegate {

  private Expression query;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String identifier = this.identifier.getValue(execution).toString();
    String query = this.query.getValue(execution).toString();

    Connection conn = connectionService.getConnection(identifier);

    List<JsonNode> output = new ArrayList<>();

    try (Statement statement = conn.createStatement()) {
      statement.execute(query);

      ResultSet results = null;
      if (statement.getUpdateCount() == -1) {
        results = statement.getResultSet();
        while (results.next()) {
          ObjectNode row = objectMapper.createObjectNode();
          ResultSetMetaData metadata = results.getMetaData();
          for (int count = 0; count < metadata.getColumnCount(); ++count) {
            String columnName = metadata.getColumnName(count);
            // TODO: consider types; int, date, boolean, string, etc.
            row.put(columnName, results.getString(columnName));
          }
        }
      }
    }

    setOutput(execution, output);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  @Override
  public Class<?> fromTask() {
    return DatabaseQueryTask.class;
  }

}
