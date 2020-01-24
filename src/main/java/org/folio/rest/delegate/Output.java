package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.VariableType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface Output {

  public abstract EmbeddedVariable getOutputVariable(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException;

  public abstract void contextCachePut(String key, Object value);

  public abstract void setOutputVariable(Expression outputVariable);

  public default void setOutput(DelegateExecution execution, Object output)
      throws JsonMappingException, JsonProcessingException {
    EmbeddedVariable variable = getOutputVariable(execution);
    String key = variable.getKey();
    VariableType type = variable.getType();
    switch (type) {
    case CACHE:
      contextCachePut(key, output);
      break;
    case LOCAL:
      execution.setVariableLocal(key, output);
      break;
    case PROCESS:
      execution.setVariable(key, output);
      break;
    default:
      break;
    }
  }

}
