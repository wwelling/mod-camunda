package org.folio.rest.delegate;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.Processor;
import org.folio.rest.workflow.model.ProcessorTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
@Scope("prototype")
public class ProcessorDelegate extends AbstractWorkflowIODelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression processor;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    Processor processor = objectMapper.readValue(this.processor.getValue(execution).toString(), Processor.class);

    String scriptName = processor.getFunctionName();

    String scriptTypeExtension = processor.getScriptType().getExtension();

    Map<String, Object> inputs = getInputs(execution);

    JsonNode input = objectMapper.valueToTree(inputs);

    String output = (String) scriptEngineService.runScript(scriptTypeExtension, scriptName, input);

    setOutput(execution, output);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setProcessor(Expression processor) {
    this.processor = processor;
  }

  @Override
  public Class<?> fromTask() {
    return ProcessorTask.class;
  }

}