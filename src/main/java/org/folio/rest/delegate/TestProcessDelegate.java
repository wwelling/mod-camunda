package org.folio.rest.delegate;

import javax.script.ScriptException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestProcessDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ScriptEngineService scriptEngineService;

  private Expression script;

  private Expression scriptType;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    FlowElement bpmnModelElemen = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElemen.getName();
    String delegateId = bpmnModelElemen.getId();

    scriptEngineService.registerScript(scriptType.getValue(execution).toString(), delegateName,
        script.getValue(execution).toString());

    System.out.println(String.format("%s STARTED", delegateName));
    streamService.map(d -> {
      try {
        d = (String) scriptEngineService.runScript(scriptType.getValue(execution).toString(), delegateName, d);
      } catch (NoSuchMethodException | ScriptException e) {
        e.printStackTrace();
      }
      return d;
    });
  }

  public void setScript(Expression script) {
    this.script = script;
  }

  public void setScriptType(Expression scriptType) {
    this.scriptType = scriptType;
  }

}
