package org.folio.rest.delegate;

import java.io.IOException;
import java.time.Instant;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StreamingRequestDelegate extends AbstractReportableDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper objectMapper;

  private Expression storageDestination;

  private final WebClient.Builder webClientBuilder;

  public StreamingRequestDelegate(WebClient.Builder webClientBuilder) {
    super();
    this.webClientBuilder = webClientBuilder;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String destinationUrl = storageDestination != null ? storageDestination.getValue(execution).toString() : OKAPI_LOCATION;

    WebClient webClient = webClientBuilder.build();

    String token = (String) execution.getVariable("token");
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    Instant start = Instant.now();

    updateReport(primaryStreamId, delegateName+" STARTED AT "+start);

    streamService.map(primaryStreamId, d -> {
      try {
        webClient
        .post()
        .uri(destinationUrl)
        .syncBody(objectMapper.readTree(d))
        .header("X-Okapi-Url", OKAPI_LOCATION)
        .header("X-Okapi-Tenant", DEFAULT_TENANT)
        .header("X-Okapi-Token", token)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToFlux(JsonNode.class)
        .subscribe();
        updateReport(primaryStreamId, "Processed: "+d);
      } catch (IOException e) {
        e.printStackTrace();
        updateReport(primaryStreamId, "Error processing: "+d);
      }
      return d;
    });
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }

}