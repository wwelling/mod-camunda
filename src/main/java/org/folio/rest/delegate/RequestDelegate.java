package org.folio.rest.delegate;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

@Service
@Scope("prototype")
public class RequestDelegate extends AbstractWorkflowIODelegate {

  @Value("${okapi.url}")
  private String OKAPI_URL;

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

    Map<String, Object> inputs = getInputs(execution);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("url", request.getUrl());
    stringLoader.putTemplate("request", request.getBodyTemplate());
    cfg.setTemplateLoader(stringLoader);

    String url = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("url"), inputs);

    String body = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("request"), inputs);

    HttpMethod method = request.getMethod();
    String accept = request.getAccept();
    String contentType = request.getContentType();

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
    headers.add("X-Okapi-Url", OKAPI_URL);

    if (token.isPresent()) {
      headers.add("X-Okapi-Token", token.get().toString());
    }

    HttpEntity<Object> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<Object> response = httpService.exchange(url, method, entity, Object.class);

    setOutput(execution, response.getBody());

    getHeaderOutputVariables(execution).forEach(headerOutputVariable -> {
      Optional<String> key = headerOutputVariable.getKey();
      if (key.isPresent()) {
        Optional<String> headerOutput = Optional.ofNullable(response.getHeaders().getFirst(key.get()));
        if (headerOutput.isPresent()) {
          Optional<VariableType> type = headerOutputVariable.getType();
          if (type.isPresent()) {
            switch (type.get()) {
            case LOCAL:
              execution.setVariableLocal(key.get(), headerOutput.get());
              break;
            case PROCESS:
              execution.setVariable(key.get(), headerOutput.get());
              break;
            default:
              break;
            }
          } else {
            logger.warn("Variable type not present for {}", key.get());
          }
        } else {
          logger.warn("Header output not present for {}", key.get());
        }
      } else {
        logger.warn("Header output key is null");
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

  public Set<EmbeddedVariable> getHeaderOutputVariables(DelegateExecution execution) throws JsonProcessingException {
    // @formatter:off
    return objectMapper.readValue(headerOutputVariables.getValue(execution).toString(),
        new TypeReference<Set<EmbeddedVariable>>() {});
    // @formatter:on
  }

}
