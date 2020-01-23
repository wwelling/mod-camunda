package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.VariableType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public abstract class AbstractWorkflowIODelegate extends AbstractWorkflowInputDelegate {

  private Expression outputVariable;

  public AbstractWorkflowIODelegate() {
    super();
  }

  public EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonMappingException, JsonProcessingException {
    return objectMapper.readValue(outputVariable.getValue(execution).toString(), EmbeddedVariable.class);
  }

  public void setOutput(DelegateExecution execution, Object output)
      throws JsonMappingException, JsonProcessingException {
    EmbeddedVariable variable = getOutputVariable(execution);
    String key = variable.getKey();
    VariableType type = variable.getType();
    switch (type) {
    case CACHE:
      contextCacheService.put(key, output);
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

  public void setOutputVariable(Expression outputVariable) {
    this.outputVariable = outputVariable;
  }

}
