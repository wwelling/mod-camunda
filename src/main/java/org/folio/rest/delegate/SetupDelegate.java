package org.folio.rest.delegate;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.EmbeddedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Scope("prototype")
public class SetupDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression initialContext;

  private Expression processors;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    logger.info("loading initial context");

    Map<String, String> context = objectMapper.readValue(initialContext.getValue(execution).toString(),
        new TypeReference<Map<String, String>>() {
        });

    for (Map.Entry<String, String> entry : context.entrySet()) {
      execution.setVariable(entry.getKey(), entry.getValue());
      logger.info("{}: {}", entry.getKey(), entry.getValue());
    }

    logger.info("loading scripts");

    List<EmbeddedProcessor> processors = objectMapper.readValue(this.processors.getValue(execution).toString(),
        new TypeReference<List<EmbeddedProcessor>>() {
        });

    for (EmbeddedProcessor processor : processors) {
      scriptEngineService.registerScript(processor.getScriptType().getExtension(), processor.getFunctionName(), processor.getCode());
      logger.info("{}: {}", processor.getFunctionName(), processor.getCode());
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setInitialContext(Expression initialContext) {
    this.initialContext = initialContext;
  }

  public void setProcessors(Expression processors) {
    this.processors = processors;
  }

}
