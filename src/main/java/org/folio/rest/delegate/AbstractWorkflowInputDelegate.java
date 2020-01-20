package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.Expression;

public abstract class AbstractWorkflowInputDelegate extends AbstractWorkflowDelegate {

  private Expression contextInputKeys;

  private Expression contextCacheInputKeys;

  public AbstractWorkflowInputDelegate() {
    super();
  }

  public Expression getContextInputKeys() {
    return contextInputKeys;
  }

  public void setContextInputKeys(Expression contextInputKeys) {
    this.contextInputKeys = contextInputKeys;
  }

  public Expression getContextCacheInputKeys() {
    return contextCacheInputKeys;
  }

  public void setContextCacheInputKeys(Expression contextCacheInputKeys) {
    this.contextCacheInputKeys = contextCacheInputKeys;
  }

}
