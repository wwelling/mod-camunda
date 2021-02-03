package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDatabaseDelegate extends AbstractWorkflowDelegate {

  Expression identifier;

  @Autowired
  DatabaseConnectionService connectionService;

  public void setIdentifier(Expression identifier) {
    this.identifier = identifier;
  }

}
