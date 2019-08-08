package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Service
@Scope("prototype")
public class OrderedMergingExtractorDelegate extends AbstractExtractorDelegate {

  @Autowired
  private StreamService streamService;

  private Expression comparisonProperty;

  public OrderedMergingExtractorDelegate(WebClient.Builder webClientBuilder) {
    super(webClientBuilder);
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    String property = comparisonProperty.getValue(execution).toString();

    Flux<String> newStream = this.getStream(execution);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");
    String newPrimaryStreamId = streamService.orderedMergeFlux(primaryStreamId, newStream, property);
    execution.setVariable("primaryStreamId", newPrimaryStreamId);

    log.info("ORDERED MERGING EXTRACTOR DELEGATE FINISHED");
  }

  public void setStreamSource(Expression streamSource) {
    this.streamSource = streamSource;
  }

  public void setComparisonProperty(Expression comparisonProperty) {
    this.comparisonProperty = comparisonProperty;
  }
}