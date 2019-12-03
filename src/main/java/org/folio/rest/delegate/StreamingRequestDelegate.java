package org.folio.rest.delegate;

import java.time.Instant;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

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

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    super.execute(execution);
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String destinationUrl = storageDestination != null ? storageDestination.getValue(execution).toString() : OKAPI_LOCATION;

    String token = (String) execution.getVariable("token");
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    updateReport(primaryStreamId, String.format("%s STARTED AT %s",delegateName, Instant.now()));

    streamService.map(primaryStreamId, d -> {
      webClient
        .post()
        .uri(destinationUrl)
        .bodyValue(d.getBytes())
        .header("X-Okapi-Url", OKAPI_LOCATION)
        .header("X-Okapi-Tenant", DEFAULT_TENANT)
        .header("X-Okapi-Token", token)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToFlux(JsonNode.class)
        .subscribe();
      updateReport(primaryStreamId, String.format("Sent POST to Storage Destination: %s", d));
      return d;
    });
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }

}