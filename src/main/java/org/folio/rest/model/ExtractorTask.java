package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class ExtractorTask extends Task {

  private String predicateProperty;

  private String enhancementProperty;

  private MergeStrategy mergeStrategy;

  public ExtractorTask() {
    super();
    setMergeStrategy(MergeStrategy.ENHANCE);
    calculateDelegateName();
  }

  public ExtractorTask(String predicateProperty, MergeStrategy mergeStrategy) {
    this();
    setPredicateProperty(predicateProperty);
    setMergeStrategy(mergeStrategy);
    calculateDelegateName();
  }

  private void calculateDelegateName() {
    switch(getMergeStrategy()) {
      case MERGE:
        setDelegate("orderedMergingExtractorDelegate");
        break;
      case ENHANCE:
        setDelegate("enhancingExtractorDelegate");
        break;
      case CONCAT:
      default:
        setDelegate("concatenatingExtractorDelegate");
        break;
    }
  }

  public String getPredicateProperty() {
    return predicateProperty;
  }

  public void setPredicateProperty(String predicateProperty) {
    this.predicateProperty = predicateProperty;
  }

  public String getEnhancementProperty() {
    return enhancementProperty;
  }

  public void setEnhancementProperty(String enhancementProperty) {
    this.enhancementProperty = enhancementProperty;
  }

  public MergeStrategy getMergeStrategy() {
    return mergeStrategy;
  }

  public void setMergeStrategy(MergeStrategy mergeStrategy) {
    this.mergeStrategy = mergeStrategy;
  }

}
