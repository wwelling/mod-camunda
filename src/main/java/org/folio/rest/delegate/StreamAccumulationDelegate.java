package org.folio.rest.delegate;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
@Scope("prototype")
public class StreamAccumulationDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper mapper;

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  private Expression accumulateTo;

  private Expression delayDuration;

  private Expression storageDestination;

  private final WebClient.Builder webClientBuilder;

  public StreamAccumulationDelegate(WebClient.Builder webClientBuilder) {
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

    String destinationBaseUrl = storageDestination != null ? storageDestination.getValue(execution).toString() : OKAPI_LOCATION;

    WebClient webClient = webClientBuilder.baseUrl(destinationBaseUrl).build();

    log.info(String.format("%s STARTED", delegateName));

    String token = (String) execution.getVariable("token");

    Instant start = Instant.now();

    List<ErrorReport> totalFailed = new ArrayList<ErrorReport>();
    AtomicInteger totalSuccesses = new AtomicInteger();

    AtomicBoolean finished = new AtomicBoolean();

    int buffer = accumulateTo != null ? Integer.parseInt(accumulateTo.getValue(execution).toString()) : 500;
    int delay = delayDuration != null ? Integer.parseInt(delayDuration.getValue(execution).toString()) : 10;

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    streamService.getFlux(primaryStreamId)
      .buffer(buffer)
      .delayElements(Duration.ofSeconds(delay))
      .doFinally(f->{
        log.info(String.format("\n\nFINISHED STREAM! %s\n\n", f.toString()));
        Instant end = Instant.now();
        log.info("TIME: " + Duration.between(start, end).getSeconds() + " seconds");
        finished.set(true);
      })
      .subscribe(rows -> {

        Instant now = Instant.now();
        log.info("TIME: " + Duration.between(start, now).getSeconds() + " seconds");

        List<ErrorReport> batchFailed = new ArrayList<ErrorReport>();
        AtomicInteger batchSuccesses = new AtomicInteger();

        rows.forEach(row -> {
          try {
            webClient
              .post()
              .uri(String.format("%s/organizations-storage/organizations", destinationBaseUrl))
              .syncBody(mapper.readTree(row))
              .header("X-Okapi-Tenant", DEFAULT_TENANT)
              .header("X-Okapi-Token", token)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .onStatus(HttpStatus::isError, err->{
                ErrorReport errorReport = new ErrorReport(
                  row.toString(),
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
                    row.toString(),
                    err.getMessage()
                  );
                  batchFailed.add(errorReport);
                  totalFailed.add(errorReport);
                }
              })
              .doOnEach(e->{
                log.info(String.format(
                  "\n%s: %s/%s (ttl %s), failure: %s/%s (ttl %s)",
                  delegateName,
                  batchSuccesses.get(),
                  rows.size(),
                  totalSuccesses.get(),
                  batchFailed.size(),
                  rows.size(),
                  totalFailed.size()
                ));
              })
              .doFinally(f->{
                Instant end = Instant.now();
                log.info("TIME: " + Duration.between(start, end).getSeconds() + " seconds");
                if(batchFailed.size()>0) {
                  log.error("ERROR EXAMPLE");
                  ErrorReport e = batchFailed.remove(0);
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
    });
  }

  public void setAccumulateTo(Expression accumulateTo) {
    this.accumulateTo = accumulateTo;
  }

  public void setDelayDuration(Expression delayDuration) {
    this.delayDuration = delayDuration;
  }

  public void setStorageDestination(Expression storageDestination) {
    this.storageDestination = storageDestination;
  }

}
