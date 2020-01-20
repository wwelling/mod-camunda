package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.Expression;

public abstract class AbstractWorkflowOutputDelegate extends AbstractWorkflowDelegate {

  private Expression outputKey;

  private Expression useCacheOutput;

  public AbstractWorkflowOutputDelegate() {
    super();
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

}
