package org.folio.rest.delegate.poc;

import static org.camunda.spin.Spin.JSON;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.folio.rest.service.OkapiRequestService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@Scope("prototype")
public class TestCreateForEachDelegate extends TestAbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  OkapiRequestService okapiRequestService;

  private Expression endpoint;

  private Expression target;

  private Expression source;

  private Expression uniqueBy;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    FlowElement bpmnModelElemen = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElemen.getName();
    if (endpoint != null && target != null && source != null) {
      String tenant = "tern";
      String token = (String) execution.getVariable("okapiToken");
      String targetValue = target.getValue(execution).toString();
      String sourceValue = source.getValue(execution).toString();
      String uniqueByValue = uniqueBy.getValue(execution).toString();
      String endpointValue = endpoint.getValue(execution).toString();

      log.info(String.format("%s STARTED", delegateName));

      streamService.map(d -> {

        System.out.print(".");

        String returnData = d;
        try {

          ObjectNode destNode = (ObjectNode) objectMapper.readTree(d);
          JsonNode srcNode = getSourceNode(sourceValue, destNode);
          ArrayNode ids = objectMapper.createArrayNode();

          if (srcNode != null && srcNode.isArray()) {
            srcNode.forEach(s -> {
              if(s.isArray()) {
                s.forEach(ss->{
                  runFindOrCreate(tenant, token, targetValue, sourceValue, uniqueByValue, endpointValue, destNode, ids, ss);
                });
              } else {
                runFindOrCreate(tenant, token, targetValue, sourceValue, uniqueByValue, endpointValue, destNode, ids, s);
              }
            });
            returnData = destNode.toString();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return returnData;
      });
    }
  }

  private void runFindOrCreate(String tenant, String token, String targetValue, String sourceValue, String uniqueByValue,
      String endpointValue, ObjectNode destNode, ArrayNode ids, JsonNode s) {
    OkapiResponse res = null;
    JsonNode rNode = null;

    try {
      if (uniqueByValue.equals("NO_VALUE")) {
        res = createEntity(tenant, token, endpointValue, s);
        rNode = objectMapper.readTree(res.getBody());
      } else {
        if (s.get(uniqueByValue) != null) {
          res = getEntity(tenant, token, endpointValue, uniqueByValue, s.get(uniqueByValue).asText());
          JsonNode rNodes = objectMapper.readTree(res.getBody());
          if (rNodes.get("totalRecords").asInt() == 0) {
            res = createEntity(tenant, token, endpointValue, s);
            rNode = objectMapper.readTree(res.getBody());
          } else {
            rNode = rNodes.fields().next().getValue().get(0);
          }
        }
      }
      if (rNode != null && rNode.get(targetValue) != null) {
        String newId = rNode.get(targetValue).toString();
        ids.add(newId.replace("\"", ""));
        setDestinationNode(sourceValue, destNode, ids);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private JsonNode getSourceNode(String sourceValue, ObjectNode destNode) throws IOException {
    JsonNode sourceNode = destNode;
    String[] sourceParts = sourceValue.split("\\/");

    for (int i=0;i<sourceParts.length;i++) {
      String part = sourceParts[i];
      if(sourceNode.isArray()) {
        ArrayNode workingArray = objectMapper.createArrayNode();
        sourceNode.forEach(n->{
          workingArray.add(n.get(part));
        });
        sourceNode = workingArray;
      } else {
        sourceNode = sourceNode.get(part);
      }
    }
    return sourceNode;
  }

  private void setDestinationNode(String sourceValue, ObjectNode destNode, ArrayNode ids) throws IOException {
    ObjectNode sourceNode = destNode;
    String[] sourceParts = sourceValue.split("\\/");

    for (int i=0;i<sourceParts.length;i++) {
      String part = sourceParts[i];
      if(i==sourceParts.length-1) {
        sourceNode.set(part, ids);
      } else{
        sourceNode=sourceNode.with(part);
      }
    }
  }

  private OkapiResponse createEntity(String tenant, String token, String endpointValue, JsonNode d) {
    OkapiRequest okapiRequest = new OkapiRequest();
    okapiRequest.setTenant(tenant);
    okapiRequest.setRequestUrl(endpointValue);
    okapiRequest.setRequestMethod("POST");
    okapiRequest.setRequestContentType("application/json");
    okapiRequest.setRequestPayload(JSON(d));
    okapiRequest.setOkapiToken(token);
    return okapiRequestService.okapiRestCall(okapiRequest);
  }

  private OkapiResponse getEntity(String tenant, String token, String endpointValue, String uniqueKey, String value) {
    OkapiRequest okapiRequest = new OkapiRequest();
    okapiRequest.setTenant(tenant);
    okapiRequest.setRequestUrl(String.format("%s?query=(%s=\"%s\")", endpointValue, uniqueKey.replaceAll("/\\//", ""), value));
    okapiRequest.setRequestMethod("GET");
    okapiRequest.setRequestPayload(JSON("{}"));
    okapiRequest.setRequestContentType("application/json");
    okapiRequest.setOkapiToken(token);
    return okapiRequestService.okapiRestCall(okapiRequest);
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

  public void setUniqueBy(Expression uniqueBy) {
    this.uniqueBy = uniqueBy;
  }

}
