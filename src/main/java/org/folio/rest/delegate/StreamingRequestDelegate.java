package org.folio.rest.delegate;

import java.time.Instant;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Scope("prototype")
public class StreamingRequestDelegate extends AbstractReportableDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private StreamService streamService;

  @Autowired
  private WebClient webClient;

  private Expression storageDestination;
  
  private Expression contentType;
  
  private Expression accept;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    super.execute(execution);
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String storageDestination = this.storageDestination != null ? this.storageDestination.getValue(execution).toString() : OKAPI_LOCATION;
    String contentType = this.contentType != null ? this.contentType.getValue(execution).toString() : MediaType.APPLICATION_JSON_VALUE;
    String accept = this.accept != null ? this.accept.getValue(execution).toString() : MediaType.APPLICATION_JSON_VALUE;

    String token = (String) execution.getVariable("token");
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    updateReport(primaryStreamId, String.format("%s STARTED AT %s",delegateName, Instant.now()));

    streamService.map(primaryStreamId, d -> {
      byte[] body = d.getBytes();
      String contentLength = String.valueOf(body.length);
      webClient
        .post()
        .uri(storageDestination)
        .bodyValue(body)
        .header("X-Okapi-Url", OKAPI_LOCATION)
        .header("X-Okapi-Tenant", DEFAULT_TENANT)
        .header("X-Okapi-Token", token)
        .header(HttpHeaders.CONTENT_TYPE, contentType)
        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
        .header(HttpHeaders.ACCEPT, accept)
        .retrieve()
        .bodyToFlux(String.class)
        .subscribe();
      updateReport(primaryStreamId, String.format("Sent POST to Storage Destination: %s", d));
      return d;
    });
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }
  
  public void setContentType(Expression contentType) {
    this.contentType = contentType;
  }

  public void setAccept(Expression accept) {
    this.accept = accept;
  }

}