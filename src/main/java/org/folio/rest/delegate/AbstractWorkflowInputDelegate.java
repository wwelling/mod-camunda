package org.folio.rest.delegate;

import java.util.Set;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

public abstract class AbstractWorkflowInputDelegate extends AbstractWorkflowDelegate implements Input {

  private Expression inputVariables;

  public AbstractWorkflowInputDelegate() {
    super();
  }

  public Set<EmbeddedVariable> getInputVariables(DelegateExecution execution) throws JsonProcessingException {
    // @formatter:off
    return objectMapper.readValue(inputVariables.getValue(execution).toString(),
        new TypeReference<Set<EmbeddedVariable>>() {});
    // @formatter:on
  }

  public void setInputVariables(Expression inputVariables) {
    this.inputVariables = inputVariables;
  }

}
