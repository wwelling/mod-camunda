package org.folio.rest.delegate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class StreamingRequestDelegate extends AbstractRuntimeDelegate {

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
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String destinationUrl = storageDestination != null ? storageDestination.getValue(execution).toString() : OKAPI_LOCATION;

    log.info("{} STARTED", delegateName);

    String token = (String) execution.getVariable("token");
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    Instant start = Instant.now();
    AtomicInteger counter = new AtomicInteger(0);

    streamService
      .toJsonNodeFlux(streamService.getFlux(primaryStreamId))
      .doFinally(r -> {
        Instant now = Instant.now();
        log.info("{} finished {} batches {} seconds\n\n", delegateName, counter.get(), Duration.between(start, now).getSeconds());
      }).subscribe(reqNode -> {
        webClient
          .post()
          .uri(destinationUrl)
          .bodyValue(reqNode)
          .header("X-Okapi-Url", OKAPI_LOCATION)
          .header("X-Okapi-Tenant", DEFAULT_TENANT)
          .header("X-Okapi-Token", token)
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToFlux(JsonNode.class)
          .subscribe();
        int cc = counter.incrementAndGet();
        if (cc % 1000 == 0) {
          log.info(reqNode.toString());
        } else {
          System.out.print(".");
        }
    });
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }

}