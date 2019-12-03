package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

public abstract class AbstractExtractorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private WebClient webClient;

  protected Expression streamSource;

  public AbstractExtractorDelegate() {
    super();
  }

  protected Flux<String> getStream(DelegateExecution execution) {
    String sourceUrl = streamSource.getValue(execution).toString();

    String delegateName = execution.getBpmnModelElementInstance().getName();
    log.info(String.format("%s STARTED", delegateName));

    String tenant = execution.getTenantId();
    String token = (String) execution.getVariable("token");

    return webClient
      .get()
      .uri(sourceUrl)
      .header("X-Okapi-Tenant", tenant)
      .header("X-Okapi-Token", token)
      .accept(MediaType.APPLICATION_STREAM_JSON)
      .retrieve()
      .bodyToFlux(String.class);
  }

  public void setStreamSource(Expression streamSource) {
    this.streamSource = streamSource;
  }

}