package org.folio.rest.delegate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.StreamRequestToDirectoryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Scope("prototype")
public class StreamRequestToDirectoryDelegate extends AbstractWorkflowInputDelegate {

  @Autowired
  private WebClient webClient;

  private Expression url;

  private Expression method;

  private Expression contentType;

  private Expression accept;

  private Expression bodyTemplate;

  private Expression path;

  private Expression workflow;

  private Expression batchSize;

  private Expression completeMessage;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String tenant = execution.getTenantId();
    String url = this.url.getValue(execution).toString();
    String method = this.method.getValue(execution).toString();
    String accept = this.accept.getValue(execution).toString();
    String contentType = this.contentType.getValue(execution).toString();

    Map<String, Object> context = new HashMap<String, Object>();

    Set<String> contextReqKeys = objectMapper.readValue(getContextInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextReqKeys.forEach(reqKey -> context.put(reqKey, execution.getVariable(reqKey)));

    Set<String> contextCacheReqKeys = objectMapper.readValue(getContextCacheInputKeys().getValue(execution).toString(),
        new TypeReference<Set<String>>() {
        });

    contextCacheReqKeys.forEach(reqKey -> {
      Optional<Object> cacheReqValue = contextCacheService.pull(reqKey);
      if (cacheReqValue.isPresent()) {
        context.put(reqKey, cacheReqValue.get());
      } else {
        logger.warn("Cannot find {} in context cache", reqKey);
      }
    });

    String path = this.path.getValue(execution).toString();

    String workflow = this.workflow.getValue(execution).toString();

    File workflowDirectory = new File(String.join(File.separator, path, tenant, workflow));

    String workflowDataPath = workflowDirectory.getAbsolutePath();

    if (!workflowDirectory.exists()) {
      if (workflowDirectory.mkdirs()) {
        logger.info("Created directory {}", workflowDataPath);
      } else {
        throw new RuntimeException(String.format("Failed to create directory %s", workflowDataPath));
      }
    }

    StringSubstitutor sub = new StringSubstitutor(context);

    String body = sub.replace(this.bodyTemplate.getValue(execution).toString());

    logger.info("url: {}", url);
    logger.debug("method: {}", method);

    logger.debug("accept: {}", accept);
    logger.debug("content-type: {}", contentType);
    logger.debug("body: {}", body);

    int batchSize = Integer.parseInt(this.batchSize.getValue(execution).toString());

    AtomicInteger count = new AtomicInteger(1);

    // @formatter:off
    webClient
      .method(HttpMethod.valueOf(method))
      .uri(url)
      .bodyValue(body.getBytes())
      .header("Accept", accept)
      .header("Content-Type", contentType)
      .header("X-Okapi-Tenant", tenant)
      .retrieve()
      .bodyToFlux(Map.class)
      .buffer(batchSize)
      .doOnComplete(() -> {
        long endTime = System.nanoTime();
        logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
        String completeMessage = this.completeMessage.getValue(execution).toString();
        logger.info("Emitting message {}", completeMessage);
        runtimeService.createMessageCorrelation(completeMessage)
          .tenantId(tenant)
          .correlate();
      })
      .subscribe(batch -> {
        String filename = StringUtils.leftPad(String.valueOf(count.getAndIncrement()), 16, "0");
        String filePath = String.join(File.separator, workflowDataPath, filename);
        logger.info("Writing file {}", filePath);
        try {
          objectMapper.writeValue(new File(filePath), batch);
        } catch (IOException e) {
          logger.error("Failed writing file {} {}", filePath, e.getMessage());
        }          
    });
    // @formatter:on
  }

  public void setWebClient(WebClient webClient) {
    this.webClient = webClient;
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

  public void setPath(Expression path) {
    this.path = path;
  }

  public void setWorkflow(Expression workflow) {
    this.workflow = workflow;
  }

  public void setBatchSize(Expression batchSize) {
    this.batchSize = batchSize;
  }

  public void setCompleteMessage(Expression completeMessage) {
    this.completeMessage = completeMessage;
  }

  @Override
  public Class<?> fromTask() {
    return StreamRequestToDirectoryTask.class;
  }

}