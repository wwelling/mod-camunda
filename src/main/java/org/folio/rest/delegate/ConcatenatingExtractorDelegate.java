package org.folio.rest.delegate;

import com.fasterxml.jackson.databind.JsonNode;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;


/*
 *  This delegate concatenates a new stream of data to the primary stream
 */
@Service
@Scope("prototype")
public class ConcatenatingExtractorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  private Expression streamSource;

  private final WebClient.Builder webClientBuilder;

  public ConcatenatingExtractorDelegate(WebClient.Builder webClientBuilder) {
    super();
    this.webClientBuilder = webClientBuilder;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    String sourceBaseUrl = streamSource != null ? streamSource.getValue(execution).toString() : OKAPI_LOCATION;

    WebClient webClient = webClientBuilder.baseUrl(sourceBaseUrl).build();

    String delegateName = execution.getBpmnModelElementInstance().getName();
    log.info(String.format("%s STARTED", delegateName));

    JsonNode payload = (JsonNode) execution.getVariable("payload");
    String extratorId = payload.get("extractorId").asText();

    String tenant = execution.getTenantId();
    String token = (String) execution.getVariable("token");

    log.info("START REQUEST");

    String fluxId = streamService.setFlux(
      webClient
        .get()
        .uri("%s/extractors/{id}/run", extratorId)
        .header("X-Okapi-Tenant", tenant)
        .header("X-Okapi-Token", token)
        .accept(MediaType.APPLICATION_STREAM_JSON)
        .retrieve()
        .bodyToFlux(String.class)
    );
    Flux<String> newStream = streamService.getFlux(fluxId);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");
    Flux<String> primaryStream = streamService.getFlux(primaryStreamId);

    Flux<String> newPrimaryStream = primaryStream.concatWith(newStream);

    String newPrimaryStreamId = streamService.setFlux(newPrimaryStream);
    execution.setVariable("primaryStreamId", newPrimaryStreamId);

    log.info("CONCATENATING EXTRACTOR DELEGATE FINISHED");
  }

  public void setStreamSource(Expression streamSource) {
    this.streamSource = streamSource;
  }

}
