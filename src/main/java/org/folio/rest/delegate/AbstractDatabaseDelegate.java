package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDatabaseDelegate extends AbstractDelegate {

  Expression name;

  @Autowired
  DatabaseConnectionService connectionService;

  public void setName(Expression name) {
    this.name = name;
  }

}
