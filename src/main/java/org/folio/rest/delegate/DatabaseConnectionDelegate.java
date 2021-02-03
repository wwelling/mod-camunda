package org.folio.rest.delegate;

import java.util.Properties;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.DatabaseConnectionTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DatabaseConnectionDelegate extends AbstractDatabaseDelegate {

  private Expression url;
  private Expression user;
  private Expression password;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String url = this.url.getValue(execution).toString();
    String key = this.designation.getValue(execution).toString();

    Properties info = new Properties();
    info.setProperty("user", user.getValue(execution).toString());
    info.setProperty("password", password.getValue(execution).toString());

    connectionService.createConnection(key, url, info);

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

  @Override
  public Class<?> fromTask() {
    return DatabaseConnectionTask.class;
  }

}
