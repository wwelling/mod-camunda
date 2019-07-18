package org.folio.rest.delegate;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.camunda.bpm.engine.delegate.Expression;

import reactor.core.publisher.Mono;

@Service
@Scope("prototype")
public class TestAccumulatorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper mapper;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  private final WebClient webClient;

  private Expression accumulateTo;

  private Expression delayDuration;

  public TestAccumulatorDelegate(WebClient.Builder webClientBuilder) {
    super();
    webClient = webClientBuilder.baseUrl(OKAPI_LOCATION).build();
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

    System.out.println(String.format("%s STARTED", delegateName));

    String token = (String) execution.getVariable("okapiToken");

    Instant start = Instant.now();

    List<ErrorReport> totalFailed = new ArrayList<ErrorReport>();
    AtomicInteger totalSuccesses = new AtomicInteger();

    AtomicBoolean finished = new AtomicBoolean();
    
    int buffer = accumulateTo != null ? Integer.parseInt(accumulateTo.getValue(execution).toString()) : 500;
    int delay = delayDuration != null ? Integer.parseInt(delayDuration.getValue(execution).toString()) : 10;

    streamService.getFlux()
      .buffer(buffer)
      .delayElements(Duration.ofSeconds(delay))
      .doFinally(f->{
        System.out.println(String.format("\n\nFINISHED STREAM! %s\n\n", f.toString()));
        Instant end = Instant.now();
        System.out.println("TIME: " + Duration.between(start, end).getSeconds() + " seconds");
        finished.set(true);
      })
      .subscribe(rows -> {

        Instant now = Instant.now();
        System.out.println("TIME: " + Duration.between(start, now).getSeconds() + " seconds");

        List<ErrorReport> batchFailed = new ArrayList<ErrorReport>();
        AtomicInteger batchSuccesses = new AtomicInteger();

        rows.forEach(row -> {
          try {
            webClient
              .post()
              .uri(String.format("%s/organizations-storage/organizations", OKAPI_LOCATION))
              .syncBody(mapper.readTree(row))
              .header("X-Okapi-Tenant", "tern")
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
                System.out.println(String.format(
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
                System.out.println("TIME: " + Duration.between(start, end).getSeconds() + " seconds");
                if(batchFailed.size()>0) {
                  System.out.println("ERROR EXAMPLE");
                  ErrorReport e = batchFailed.remove(0);
                  System.out.println(e.errorMessage);
                  System.out.println(e.object);
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

}
