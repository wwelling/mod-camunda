package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

public abstract class AbstractExtractorDelegate extends AbstractRuntimeDelegate {

  protected Expression streamSource;

  protected final WebClient.Builder webClientBuilder;

  public AbstractExtractorDelegate(WebClient.Builder webClientBuilder) {
    super();
    this.webClientBuilder = webClientBuilder;
  }

  protected Flux<String> getStream(DelegateExecution execution) {
  String sourceUrl = streamSource.getValue(execution).toString();

  WebClient webClient = webClientBuilder.build();

  String delegateName = execution.getBpmnModelElementInstance().getName();
  log.info(String.format("%s STARTED", delegateName));

  String tenant = execution.getTenantId();
  String token = (String) execution.getVariable("token");

  log.info("START REQUEST");

  return webClient
    .get()
    .uri(sourceUrl)
    .header("X-Okapi-Tenant", tenant)
    .header("X-Okapi-Token", token)
    .accept(MediaType.APPLICATION_STREAM_JSON)
    .retrieve()
    .bodyToFlux(String.class);
  }
}