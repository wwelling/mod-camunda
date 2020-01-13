package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.ProcessorTask;
import org.folio.rest.workflow.model.TaskScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ProcessorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression script;

  private Expression scriptType;

  private Expression contextInputKey;

  private Expression contextOutputKey;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String scriptTypeExtension = TaskScriptType.valueOf(scriptType.getValue(execution).toString()).getExtension();

    scriptEngineService.registerScript(scriptTypeExtension, delegateName, script.getValue(execution).toString());

    String inputKey = contextInputKey.getValue(execution).toString();

    String outputKey = contextOutputKey.getValue(execution).toString();

    String input = (String) execution.getVariable(inputKey);

    String output = (String) scriptEngineService.runScript(scriptTypeExtension, delegateName, input);

    execution.setVariable(outputKey, output);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setScript(Expression script) {
    this.script = script;
  }

  public void setScriptType(Expression scriptType) {
    this.scriptType = scriptType;
  }

  public void setScriptEngineService(ScriptEngineService scriptEngineService) {
    this.scriptEngineService = scriptEngineService;
  }

  public void setContextInputKey(Expression contextInputKey) {
    this.contextInputKey = contextInputKey;
  }

  public void setContextOutputKey(Expression contextOutputKey) {
    this.contextOutputKey = contextOutputKey;
  }

  @Override
  public Class<?> fromTask() {
    return ProcessorTask.class;
  }

}