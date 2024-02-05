package org.folio.rest.camunda.delegate;

import static org.camunda.spin.Spin.JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.variable.Variables;
import org.folio.rest.workflow.enums.VariableType;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.slf4j.Logger;

public interface Output {

  public abstract Logger getLogger();

  public abstract ObjectMapper getObjectMapper();

  public abstract EmbeddedVariable getOutputVariable(DelegateExecution execution) throws JsonProcessingException;

  public abstract void setOutputVariable(Expression outputVariable);

  public default void setOutput(DelegateExecution execution, Object output) throws JsonProcessingException {
    EmbeddedVariable variable = getOutputVariable(execution);
    Optional<String> key = Optional.of(variable.getKey());
    if (key.isPresent()) {
      if (Objects.nonNull(output)) {
        Optional<VariableType> type = Optional.of(variable.getType());
        Object value = variable.isSpin()
          ? JSON(getObjectMapper().writeValueAsString(output))
          : Variables.objectValue(output, variable.getAsTransient()).create();
        if (type.isPresent()) {
          switch (type.get()) {
          case LOCAL:
            execution.setVariableLocal(key.get(), value);
            break;
          case PROCESS:
            execution.setVariable(key.get(), value);
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
