package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Service
public class EnhancingExtractorDelegate extends AbstractExtractorDelegate {

  @Autowired
  private StreamService<String> streamService;

  private Expression comparisonProperty;

  private Expression enhancementProperty;

  public EnhancingExtractorDelegate(WebClient.Builder webClientBuilder) {
    super(webClientBuilder);
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    String property = comparisonProperty.getValue(execution).toString();
    String enhancement = enhancementProperty.getValue(execution).toString();

    Flux<String> newStream = this.getStream(execution);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    streamService.enhanceFlux(primaryStreamId, newStream, property, enhancement);

  }

  public void setComparisonProperty(Expression comparisonProperty) {
    this.comparisonProperty = comparisonProperty;
  }

  public void setEnhancementProperty(Expression enhancemenetProperty) {
    this.enhancementProperty = enhancemenetProperty;
  }

}