package org.folio.rest.delegate;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Scope("prototype")
public class SetupDelegate extends AbstractDelegate {

  private Expression initialContext;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    Map<String, String> context = objectMapper.readValue(initialContext.getValue(execution).toString(),
      new TypeReference<Map<String, String>>() {});

    for (Map.Entry<String, String> entry : context.entrySet()) {
      execution.setVariable(entry.getKey(), entry.getValue());
      logger.info("{}: {}", entry.getKey(), entry.getValue());
    }
    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setInitialContext(Expression initialContext) {
    this.initialContext = initialContext;
  }

}
