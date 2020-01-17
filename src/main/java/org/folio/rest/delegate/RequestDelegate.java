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
public class RequestDelegate extends AbstractWorkflowDelegate {

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

    logger.info("{} started", delegateName);

    String reqUrl = url.getValue(execution).toString();
    String reqMethod = method.getValue(execution).toString();
    String reqAccept = accept.getValue(execution).toString();
    String reqContentType = contentType.getValue(execution).toString();

    Map<String, Object> reqContext = new HashMap<String, Object>();

    Set<String> contextReqKeys = objectMapper.readValue(getContextInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextReqKeys.forEach(reqKey -> reqContext.put(reqKey, execution.getVariable(reqKey)));

    Set<String> contextCacheReqKeys = objectMapper.readValue(getContextCacheInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextCacheReqKeys.forEach(reqKey -> {
      Optional<Object> cacheReqValue = contextCacheService.pull(reqKey);
      if (cacheReqValue.isPresent()) {
        reqContext.put(reqKey, cacheReqValue.get());
      } else {
        logger.warn("Cannot find %s in context cache", reqKey);
      }
    });

    StringSubstitutor sub = new StringSubstitutor(reqContext);

    String reqBody = sub.replace(bodyTemplate.getValue(execution).toString());

    logger.info("url: {}", reqUrl);
    logger.debug("method: {}", reqMethod);

    logger.debug("accept: {}", reqAccept);
    logger.debug("content-type: {}", reqContentType);
    logger.debug("body: {}", reqBody);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", reqAccept);
    headers.add("Content-Type", reqContentType);

    HttpEntity<Object> entity = new HttpEntity<Object>(reqBody, headers);
    ResponseEntity<Object> response = httpService.exchange(reqUrl, HttpMethod.valueOf(reqMethod), entity, Object.class);

    boolean useCacheOutput = Boolean.parseBoolean(getUseCacheOutput().getValue(execution).toString().trim());

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