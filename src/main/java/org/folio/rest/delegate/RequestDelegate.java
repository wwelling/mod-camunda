package org.folio.rest.delegate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.RequestTask;
import org.folio.spring.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Scope("prototype")
public class RequestDelegate extends AbstractWorkflowIODelegate {

  @Autowired
  private HttpService httpService;

  private Expression url;

  private Expression method;

  private Expression contentType;

  private Expression accept;

  private Expression bodyTemplate;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    String tenant = execution.getTenantId();

    logger.info("{} started", delegateName);

    String url = this.url.getValue(execution).toString();
    String method = this.method.getValue(execution).toString();
    String accept = this.accept.getValue(execution).toString();
    String contentType = this.contentType.getValue(execution).toString();

    Map<String, Object> reqContext = new HashMap<String, Object>();

    Set<String> contextKeys = objectMapper.readValue(getContextInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextKeys.forEach(reqKey -> reqContext.put(reqKey, execution.getVariable(reqKey)));

    Set<String> contextCacheKeys = objectMapper.readValue(getContextCacheInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextCacheKeys.forEach(key -> {
      Optional<Object> cacheValue = contextCacheService.pull(key);
      if (cacheValue.isPresent()) {
        reqContext.put(key, cacheValue.get());
      } else {
        logger.warn("Cannot find {} in context cache", key);
      }
    });

    StringSubstitutor sub = new StringSubstitutor(reqContext);

    String body = sub.replace(bodyTemplate.getValue(execution).toString());

    logger.info("url: {}", url);
    logger.debug("method: {}", method);

    logger.debug("accept: {}", accept);
    logger.debug("content-type: {}", contentType);
    logger.debug("body: {}", body);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", accept);
    headers.add("Content-Type", contentType);
    headers.add("X-Okapi-Tenant", tenant);

    HttpEntity<Object> entity = new HttpEntity<Object>(body, headers);
    ResponseEntity<Object> response = httpService.exchange(url, HttpMethod.valueOf(method), entity, Object.class);

    boolean useCacheOutput = Boolean.parseBoolean(getUseCacheOutput().getValue(execution).toString());

    String outputKey = getOutputKey().getValue(execution).toString();

    if (useCacheOutput) {
      contextCacheService.put(outputKey, response.getBody());
    } else {
      execution.setVariable(outputKey, response.getBody());
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setUrl(Expression url) {
    this.url = url;
  }

  public void setMethod(Expression method) {
    this.method = method;
  }

  public void setContentType(Expression contentType) {
    this.contentType = contentType;
  }

  public void setAccept(Expression accept) {
    this.accept = accept;
  }

  public void setBodyTemplate(Expression bodyTemplate) {
    this.bodyTemplate = bodyTemplate;
  }

  @Override
  public Class<?> fromTask() {
    return RequestTask.class;
  }

}