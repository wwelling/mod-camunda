package org.folio.rest.delegate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StreamingRequestDelegate extends AbstractRuntimeDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private StreamService streamService;

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

    log.info("{} started", delegateName);

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
        int cc = counter.incrementAndGet();

        if (cc % 1000 == 0) {
          log.info("TO STRING {}", reqNode.toString());
        } else {
          System.out.print(".");
        }

        if (log.isDebugEnabled()) {
          log.debug("{}", reqNode.toString());
        }
        webClient
          .post()
          .uri(destinationUrl)
          .syncBody(reqNode)
          .header("X-Okapi-Tenant", DEFAULT_TENANT)
          .header("X-Okapi-Token", token)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToFlux(JsonNode.class)
          .subscribe();
    });
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }

}