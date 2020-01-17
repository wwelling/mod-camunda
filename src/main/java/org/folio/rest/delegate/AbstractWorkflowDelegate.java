package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.ContextCacheService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractWorkflowDelegate extends AbstractRuntimeDelegate {

  @Autowired
  protected ContextCacheService contextCacheService;

  private Expression contextInputKeys;

  private Expression contextCacheInputKeys;

  private Expression outputKey;

  private Expression useCacheOutput;

  public AbstractWorkflowDelegate() {
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

  public Expression getOutputKey() {
    return outputKey;
  }

  public void setOutputKey(Expression outputKey) {
    this.outputKey = outputKey;
  }

  public Expression getUseCacheOutput() {
    return useCacheOutput;
  }

  public void setUseCacheOutput(Expression useCacheOutput) {
    this.useCacheOutput = useCacheOutput;
  }

  public abstract Class<?> fromTask();

}
