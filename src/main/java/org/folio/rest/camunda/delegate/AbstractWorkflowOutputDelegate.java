package org.folio.rest.camunda.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;

public abstract class AbstractWorkflowOutputDelegate extends AbstractWorkflowDelegate implements Output {

  private Expression outputVariable;

  protected AbstractWorkflowOutputDelegate() {
    super();
  }

  public EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException {
    return objectMapper.readValue(outputVariable.getValue(execution).toString(), EmbeddedVariable.class);
  }

  public boolean hasOutputVariable(DelegateExecution execution) {
    return Objects.nonNull(outputVariable) && Objects.nonNull(outputVariable.getValue(execution));
  }

  public void setOutputVariable(Expression outputVariable) {
    this.outputVariable = outputVariable;
  }

}
