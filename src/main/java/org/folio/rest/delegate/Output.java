package org.folio.rest.delegate;

import static org.camunda.spin.Spin.JSON;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.VariableType;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Output {

  public abstract Logger getLogger();

  public abstract ObjectMapper getObjectMapper();

  public abstract EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException;

  public abstract void setOutputVariable(Expression outputVariable);

  public default void setOutput(DelegateExecution execution, Object output) throws JsonProcessingException {
    EmbeddedVariable variable = getOutputVariable(execution);
    Optional<String> key = variable.getKey();
    if (key.isPresent()) {
      Optional<Object> value = Optional.ofNullable(output);
      if (value.isPresent()) {
        Optional<VariableType> type = variable.getType();
        if (variable.isSpin()) {
          value = Optional.ofNullable(JSON(getObjectMapper().writeValueAsString(output)));
        }
        if (type.isPresent()) {
          switch (type.get()) {
          case LOCAL:
            execution.setVariableLocal(key.get(), value.get());
            break;
          case PROCESS:
            execution.setVariable(key.get(), value.get());
            break;
          default:
            break;
          }
        } else {
          getLogger().warn("Variable type not present for {}", key.get());
        }
      } else {
        getLogger().warn("Output not present for {}", key.get());
      }
    } else {
      getLogger().warn("Output key is null");
    }
  }

}
