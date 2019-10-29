package org.folio.rest.delegate;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class StreamingRequestDelegate extends AbstractRuntimeDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private StreamService streamService;

  private Expression storageDestination;

  private final WebClient.Builder webClientBuilder;

  public StreamingRequestDelegate(WebClient.Builder webClientBuilder) {
    super();
    this.webClientBuilder = webClientBuilder;
  }

  private class ErrorReport {
    String object;
    String errorMessage;
    ErrorReport(String o, String e) {
      object = o;
      errorMessage = e;
    }
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String destinationUrl = storageDestination != null ? storageDestination.getValue(execution).toString() : OKAPI_LOCATION;

    WebClient webClient = webClientBuilder.build();

    log.info(String.format("%s STARTED", delegateName));

    Instant start = Instant.now();
    AtomicInteger totalSuccesses = new AtomicInteger();
    String token = (String) execution.getVariable("token");
    List<ErrorReport> totalFailed = new ArrayList<ErrorReport>();
    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    streamService.getFlux(primaryStreamId).subscribe(d -> {

      Instant now = Instant.now();
      log.info("TIME: " + Duration.between(start, now).getSeconds() + " seconds");

      List<ErrorReport> batchFailed = new ArrayList<ErrorReport>();
      AtomicInteger batchSuccesses = new AtomicInteger();

      try {
        JsonNode rowNode = mapper.readTree(d);
        log.debug(String.format("%s", rowNode));
        webClient
          .post()
          .uri(destinationUrl)
          .syncBody(rowNode)
          .header("X-Okapi-Tenant", DEFAULT_TENANT)
          .header("X-Okapi-Token", token)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .onStatus(HttpStatus::isError, err->{
            ErrorReport errorReport = new ErrorReport(
              err.toString(),
              err.statusCode().toString()
            );
            batchFailed.add(errorReport);
            totalFailed.add(errorReport);
            return Mono.error(new Exception("STATUS_ERROR"));
          })
          .bodyToFlux(JsonNode.class)
          .doOnError(Exception.class, err->{
            if(!err.getMessage().equals("STATUS_ERROR")) {
              ErrorReport errorReport = new ErrorReport(
                err.toString(),
                err.getMessage()
              );
              batchFailed.add(errorReport);
              totalFailed.add(errorReport);
            }
          })
          .doOnEach(e->{
            log.debug(String.format(
              "\n%s: %s (ttl %s), failure: %s (ttl %s)",
              delegateName,
              batchSuccesses.get(),
              totalSuccesses.get(),
              batchFailed.size(),
              totalFailed.size()
            ));
          })
          .doFinally(f->{
            Instant end = Instant.now();
            log.debug("TIME: " + Duration.between(start, end).getSeconds() + " seconds");
            if(totalFailed.size()>0) {
              log.error("ERROR EXAMPLE");
              ErrorReport e = totalFailed.remove(0);
              log.error(e.errorMessage);
              log.error(e.object);
            }
          })
          .subscribe(res -> {
            totalSuccesses.incrementAndGet();
            batchSuccesses.incrementAndGet();
          });
      } catch (IOException e) {
        e.printStackTrace();
      }
  });
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }

}