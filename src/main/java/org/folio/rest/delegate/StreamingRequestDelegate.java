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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Service
public class StreamingRequestDelegate extends AbstractRuntimeDelegate {

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private StreamService streamService;

  private Expression storageDestination;

  private final WebClient webClient;

  public StreamingRequestDelegate(WebClient.Builder webClientBuilder) {
    super();    
    TcpClient tcpClient = TcpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000) // connection timeout
      .doOnConnected(connection -> {
        connection
          .addHandlerLast(new ReadTimeoutHandler(3600)) // read timeout in seconds
          .addHandlerLast(new WriteTimeoutHandler(3600)); // write timeout in seconds
      });
    webClient = WebClient.builder()
      .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
      .build();
  }

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