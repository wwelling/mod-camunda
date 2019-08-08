package org.folio.rest.delegate;

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
 *  This delegate concatenates a new stream of data to the end of the primary stream
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

    String sourceUrl = streamSource.getValue(execution).toString();

    WebClient webClient = webClientBuilder.build();

    String delegateName = execution.getBpmnModelElementInstance().getName();
    log.info(String.format("%s STARTED", delegateName));

    String tenant = execution.getTenantId();
    String token = (String) execution.getVariable("token");

    log.info("START REQUEST");

    Flux<String> newStream = webClient
      .get()
      .uri(sourceUrl)
      .header("X-Okapi-Tenant", tenant)
      .header("X-Okapi-Token", token)
      .accept(MediaType.APPLICATION_STREAM_JSON)
      .retrieve()
      .bodyToFlux(String.class);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    String newPrimaryStreamId = streamService.concatenateFlux(primaryStreamId, newStream);
    execution.setVariable("primaryStreamId", newPrimaryStreamId);

    log.info("CONCATENATING EXTRACTOR DELEGATE FINISHED");
  }

  public void setStreamSource(Expression streamSource) {
    this.streamSource = streamSource;
  }

}
