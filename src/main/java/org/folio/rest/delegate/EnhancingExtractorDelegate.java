package org.folio.rest.delegate;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.folio.rest.workflow.components.EnhancementComparison;
import org.folio.rest.workflow.components.EnhancementMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Service
@Scope("prototype")
public class EnhancingExtractorDelegate extends AbstractExtractorDelegate {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StreamService streamService;

  private Expression comparisons;

  private Expression mappings;

  public EnhancingExtractorDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String comparisonsSerialized = comparisons.getValue(execution).toString();
    String mappingsSerialized = mappings.getValue(execution).toString();

    Flux<String> newStream = this.getStream(execution);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    List<EnhancementComparison> enhancementComparisons = objectMapper.readValue(comparisonsSerialized,
      new TypeReference<List<EnhancementComparison>>() {});
    List<EnhancementMapping> enhancementMappings = objectMapper.readValue(mappingsSerialized,
      new TypeReference<List<EnhancementMapping>>() {});

    streamService.enhanceFlux(primaryStreamId, newStream, enhancementComparisons, enhancementMappings);
  }

  public void setComparisons(Expression comparisons) {
    this.comparisons = comparisons;
  }

  public void setMappings(Expression mappings) {
    this.mappings = mappings;
  }

}
