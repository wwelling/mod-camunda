package org.folio.rest.delegate;

import javax.script.ScriptException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class StreamingProcessDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  @Autowired
  private StreamService streamService;

  private Expression script;

  private Expression scriptType;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    if (scriptType != null && script != null) {
      String scriptTypeValue = scriptType.getValue(execution).toString();

      scriptEngineService.registerScript(scriptTypeValue, delegateName, script.getValue(execution).toString());

      String primaryStreamId = (String) execution.getVariable("primaryStreamId");

      log.info(String.format("%s STARTED", delegateName));
      streamService.map(primaryStreamId, d -> {

        try {
          d = (String) scriptEngineService.runScript(scriptTypeValue, delegateName, d);
        } catch (NoSuchMethodException | ScriptException e) {
          e.printStackTrace();
        }

        return d;
      });
    }
  }

  public void setScript(Expression script) {
    this.script = script;
  }

  public void setScriptType(Expression scriptType) {
    this.scriptType = scriptType;
  }

}
