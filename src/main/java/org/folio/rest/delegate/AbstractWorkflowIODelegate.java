package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public abstract class AbstractWorkflowIODelegate extends AbstractWorkflowInputDelegate implements Output {

  private Expression outputVariable;

  public AbstractWorkflowIODelegate() {
    super();
  }

  public EmbeddedVariable getOutputVariable(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException {
    return objectMapper.readValue(outputVariable.getValue(execution).toString(), EmbeddedVariable.class);
  }

  public void setOutputVariable(Expression outputVariable) {
    this.outputVariable = outputVariable;
  }

}
