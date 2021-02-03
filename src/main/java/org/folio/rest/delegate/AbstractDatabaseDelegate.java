package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.DatabaseConnectionService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDatabaseDelegate extends AbstractWorkflowDelegate {

  Expression designation;

  @Autowired
  DatabaseConnectionService connectionService;

  public void setDesignation(Expression designation) {
    this.designation = designation;
  }

}
