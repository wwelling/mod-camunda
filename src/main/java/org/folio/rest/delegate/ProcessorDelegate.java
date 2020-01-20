package org.folio.rest.delegate;

import java.util.Map;

import org.apache.commons.text.CaseUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.Process;
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

  private Expression process;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    Process process = objectMapper.readValue(this.process.getValue(execution).toString(), Process.class);

    String scriptTypeExtension = process.getScriptType().getExtension();

    Map<String, Object> inputs = getInputs(execution);

    JsonNode input = objectMapper.valueToTree(inputs);

    String scriptName = CaseUtils.toCamelCase(delegateName, false, ' ');

    String output = (String) scriptEngineService.runScript(scriptTypeExtension, scriptName, input);

    setOutput(execution, output);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setProcess(Expression process) {
    this.process = process;
  }

  @Override
  public Class<?> fromTask() {
    return ProcessorTask.class;
  }

}