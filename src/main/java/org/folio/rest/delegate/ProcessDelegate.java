package org.folio.rest.delegate;

import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.workflow.model.RequestTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Scope("prototype")
public class ProcessDelegate extends AbstractRuntimeDelegate {

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

    String scriptTypeValue = scriptType.getValue(execution).toString();

    scriptEngineService.registerScript(scriptTypeValue, delegateName, script.getValue(execution).toString());

    String inputKey = contextInputKey.getValue(execution).toString();

    String outputKey = contextOutputKey.getValue(execution).toString();

    List<String> input = objectMapper.readValue((String) execution.getVariable(inputKey),
        new TypeReference<List<String>>() {});

    List<String> output = input.parallelStream().map(d -> {
      try {
        d = (String) scriptEngineService.runScript(scriptTypeValue, delegateName, d);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (ScriptException e) {
        e.printStackTrace();
      }
      return d;
    }).collect(Collectors.toList());

    execution.setVariable(outputKey, objectMapper.writeValueAsString(output));

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
    return RequestTask.class;
  }

}