package org.folio.rest.delegate;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.dto.Request;
import org.folio.rest.workflow.model.EmbeddedVariable;
import org.folio.rest.workflow.model.RequestTask;
import org.folio.rest.workflow.model.VariableType;
import org.folio.spring.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
@Scope("prototype")
public class RequestDelegate extends AbstractWorkflowIODelegate {

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private HttpService httpService;

  private Expression request;

  private Expression headerOutputVariables;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    Request request = objectMapper.readValue(this.request.getValue(execution).toString(), Request.class);

    String url = request.getUrl();
    HttpMethod method = request.getMethod();
    String accept = request.getAccept();
    String contentType = request.getContentType();

    Map<String, Object> inputs = getInputs(execution);

    StringSubstitutor sub = new StringSubstitutor(inputs);

    String body = sub.replace(request.getBodyTemplate());

    String tenant = execution.getTenantId();

    Optional<Object> token = Optional.ofNullable(execution.getVariable("X-Okapi-Token"));

    logger.info("url: {}", url);
    logger.debug("method: {}", method);

    logger.debug("accept: {}", accept);
    logger.debug("content-type: {}", contentType);
    logger.debug("tenant: {}", tenant);

    logger.debug("body: {}", body);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", accept);
    headers.add("Content-Type", contentType);
    headers.add("X-Okapi-Tenant", tenant);
    headers.add("X-Okapi-Url", OKAPI_LOCATION);

    if (token.isPresent()) {
      headers.add("X-Okapi-Token", token.get().toString());
    }

    HttpEntity<Object> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<Object> response = httpService.exchange(url, method, entity, Object.class);

    setOutput(execution, response.getBody());

    getHeaderOutputVariables(execution).forEach(headerOutputVariable -> {
      String key = headerOutputVariable.getKey();
      Optional<String> headerOutput = Optional.ofNullable(response.getHeaders().getFirst(key));
      if (headerOutput.isPresent()) {
        VariableType type = headerOutputVariable.getType();
        switch (type) {
        case CACHE:
          contextCachePut(key, headerOutput.get());
          break;
        case LOCAL:
          execution.setVariableLocal(key, headerOutput.get());
          break;
        case PROCESS:
          execution.setVariable(key, headerOutput.get());
          break;
        default:
          break;
        }
      }
    });

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setRequest(Expression request) {
    this.request = request;
  }

  public void setHeaderOutputVariables(Expression headerOutputVariables) {
    this.headerOutputVariables = headerOutputVariables;
  }

  @Override
  public Class<?> fromTask() {
    return RequestTask.class;
  }

  public Set<EmbeddedVariable> getHeaderOutputVariables(DelegateExecution execution)
      throws JsonMappingException, JsonProcessingException {
    // @formatter:off
    return objectMapper.readValue(headerOutputVariables.getValue(execution).toString(),
        new TypeReference<Set<EmbeddedVariable>>() {});
    // @formatter:on
  }

}