package org.folio.rest.camunda.delegate;

import java.util.Properties;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.DatabaseConnectionTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DatabaseConnectionDelegate extends AbstractDatabaseDelegate {

  private Expression url;
  private Expression username;
  private Expression password;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    String urlValue = this.url.getValue(execution).toString();
    String key = this.designation.getValue(execution).toString();

    Properties info = new Properties();
    info.setProperty("user", this.username.getValue(execution).toString());
    info.setProperty("password", this.password.getValue(execution).toString());

    connectionService.createPool(key, urlValue, info);

    determineEndTime(execution, startTime);
  }

  public void setUrl(Expression url) {
    this.url = url;
  }

  public void setUsername(Expression username) {
    this.username = username;
  }

  public void setPassword(Expression password) {
    this.password = password;
  }

  @Override
  public Class<?> fromTask() {
    return DatabaseConnectionTask.class;
  }

}
