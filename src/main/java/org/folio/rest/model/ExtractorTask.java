package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class ExtractorTask extends Task {

  private String predicateProperty;

  private String enhancementProperty;

  private MergeStrategy mergeStrategy;

  public ExtractorTask() {
    super();
  }

  public ExtractorTask(String predicateProperty, MergeStrategy mergeStrategy) {
    this();
    setPredicateProperty(predicateProperty);
    setMergeStrategy(mergeStrategy);
  }

  @Override
  public String getDelegate() {
    switch(getMergeStrategy()) {
      case MERGE:
        return "orderedMergingExtractorDelegate";
      case ENHANCE:
        return "enhancingExtractorDelegate";
      case CONCAT:
      default:
        return "concatenatingExtractorDelegate";
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
