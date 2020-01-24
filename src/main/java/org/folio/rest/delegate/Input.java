package org.folio.rest.delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.VariableType;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface Input {

  public abstract Logger getLogger();

  public abstract Set<EmbeddedVariable> getInputVariables(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException;

  public abstract void setInputVariables(Expression inputVariables);

  public abstract Optional<Object> contextCachePull(String key);

  public default Map<String, Object> getInputs(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException {
    Map<String, Object> inputs = new HashMap<String, Object>();
    getInputVariables(execution).forEach(variable -> {
      String key = variable.getKey();
      VariableType type = variable.getType();
      Optional<Object> value = Optional.empty();
      switch (type) {
      case CACHE:
        value = contextCachePull(key);
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
        getLogger().warn("Could not find value for {} from {}", key, type);
      }
    });
    return inputs;
  }

}
