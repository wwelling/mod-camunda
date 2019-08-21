package org.folio.rest.model;

import javax.persistence.Entity;

import org.python.jline.internal.Log;

@Entity
public class ExtractorTask extends Task {

  private String predicateProperty;

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
        Log.info("GOT HERER");
        break;
    }
  }

  public String getPredicateProperty() {
    return predicateProperty;
  }

  public void setPredicateProperty(String predicateProperty) {
    this.predicateProperty = predicateProperty;
  }

  public MergeStrategy getMergeStrategy() {
    return mergeStrategy;
  }

  public void setMergeStrategy(MergeStrategy mergeStrategy) {
    this.mergeStrategy = mergeStrategy;
  }

}
