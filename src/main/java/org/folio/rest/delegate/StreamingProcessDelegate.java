package org.folio.rest.delegate;

import java.util.List;

import javax.script.ScriptException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.service.ScriptEngineService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@Scope("prototype")
public class StreamingProcessDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private ScriptEngineService scriptEngineService;

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper objectMapper;

  private Expression script;

  private Expression scriptType;

  private Expression contextProperties;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    if (scriptType != null && script != null) {
      String scriptTypeValue = scriptType.getValue(execution).toString();

      scriptEngineService.registerScript(scriptTypeValue, delegateName, script.getValue(execution).toString());

      String primaryStreamId = (String) execution.getVariable("primaryStreamId");

      String serializedContextProperties = contextProperties.getValue(execution).toString();
      List<String> properties = objectMapper.readValue(serializedContextProperties, new TypeReference<List<String>>() {});

      log.info(String.format("%s started", delegateName));

      streamService.map(primaryStreamId, d -> {

        if (!properties.isEmpty()) {
          try {
            ObjectNode node = (ObjectNode) objectMapper.readTree(d);
            properties.forEach(p -> node.put(p, (String) execution.getVariable(p)));
            d = objectMapper.writeValueAsString(node);
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        }

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

  public void setContextProperties(Expression contextProperties) {
    this.contextProperties = contextProperties;
  }

}
