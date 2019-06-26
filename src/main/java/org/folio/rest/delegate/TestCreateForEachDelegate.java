package org.folio.rest.delegate;

import java.io.IOException;

import javax.script.ScriptException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class TestCreateForEachDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper objectMapper;

  private Expression endpoint;

  private Expression target;

  private Expression source;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    FlowElement bpmnModelElemen = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElemen.getName();
    String delegateId = bpmnModelElemen.getId();

    if (endpoint != null && target != null && source != null) {
      String endpointValue = endpoint.getValue(execution).toString();
      String targetValue = target.getValue(execution).toString();
      String sourceValue = source.getValue(execution).toString();

      System.out.println(String.format("%s STARTED", delegateName));

      streamService.map(d -> {
        try {
          JsonNode dNode = objectMapper.readTree(d);
          JsonNode sourceNode = dNode.get(sourceValue);
          if(sourceNode.isArray()) {
            sourceNode.forEach(s->{
              System.out.println(s);
            });
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        return d;
      });
    }
  }

  public void setEndpoint(Expression endpoint) {
    this.endpoint = endpoint;
  }

  public void setTarget(Expression target) {
    this.target = target;
  }

  public void setSource(Expression source) {
    this.source = source;
  }

}
