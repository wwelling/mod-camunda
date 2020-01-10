package org.folio.rest.delegate;

import java.util.List;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.folio.rest.workflow.components.EnhancementComparison;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class OrderedMergingExtractorDelegate extends AbstractExtractorDelegate {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StreamService streamService;

  private Expression comparisons;

  public OrderedMergingExtractorDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    String comparisonsSerialized = comparisons.getValue(execution).toString();

    Stream<String> newStream = this.getStream(execution);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    List<EnhancementComparison> enhancementComparisons = objectMapper.readValue(comparisonsSerialized, new TypeReference<List<EnhancementComparison>>() {});

    streamService.orderedMergeStream(primaryStreamId, newStream, enhancementComparisons);
  }

  public void setComparisons(Expression comparisons) {
    this.comparisons = comparisons;
  }

}
