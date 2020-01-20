package org.folio.rest.delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.Variable;
import org.folio.rest.workflow.model.VariableType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public abstract class AbstractWorkflowInputDelegate extends AbstractWorkflowDelegate {

  private Expression inputVariables;

  public AbstractWorkflowInputDelegate() {
    super();
  }

  public Set<Variable> getInputVariables(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException {
    return objectMapper.readValue(inputVariables.getValue(execution).toString(), new TypeReference<Set<Variable>>() {
    });
  }

  public Map<String, Object> getInputs(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException {
    Map<String, Object> inputs = new HashMap<String, Object>();
    getInputVariables(execution).forEach(variable -> {
      String key = variable.getKey();
      VariableType type = variable.getType();
      Optional<Object> value = Optional.empty();
      switch (type) {
      case CACHE:
        value = contextCacheService.pull(key);
        break;
      case LOCAL:
        value = Optional.ofNullable(execution.getVariableLocal(key));
        break;
      case PROCESS:
        value = Optional.ofNullable(execution.getVariable(key));
        break;
      default:
        break;
      }
      if (value.isPresent()) {
        inputs.put(key, value.get());
      } else {
        logger.warn("Could not find value for {} from {}", key, type);
      }
    });
    return inputs;
  }

  public void setInputVariables(Expression inputVariables) {
    this.inputVariables = inputVariables;
  }

}
