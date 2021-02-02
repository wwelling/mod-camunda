package org.folio.rest.delegate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseConnectionDelegate extends AbstractDelegate {

  private Expression url;
  private Expression user;
  private Expression password;
  private Expression name;

  @Autowired
  private DatabaseConnectionService connectionService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    String urlString = url.getValue(execution).toString();
    String userString = user.getValue(execution).toString();
    String passwordString = password.getValue(execution).toString();
    String nameString = name.getValue(execution).toString();
    Connection conn = DriverManager.getConnection(urlString, userString, passwordString);

    String identifier = UUID.randomUUID().toString();

    connectionService.addConnection(identifier, conn);

    execution.setVariable(nameString, identifier);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setUrl(Expression url) {
    this.url = url;
  }

  public void setUser(Expression user) {
    this.user = user;
  }

  public void setPassword(Expression password) {
    this.password = password;
  }

  public void setName(Expression name) {
    this.name = name;
  }

}
