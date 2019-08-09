package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class ExtractorTask extends Task {

  private String streamSource;

  private String predicateProperty;

  private MergeStrategy mergeStrategy;

  public ExtractorTask() {
    super();
  }

  public ExtractorTask(String predicateProperty, MergeStrategy mergeStrategy) {
    this();
    this.predicateProperty = predicateProperty;
    this.mergeStrategy = mergeStrategy;
    calculateDelegateName();
  }

  private void calculateDelegateName() {
    switch(getMergeStrategy()) {
      case CONCAT:
        setDelegate("concatenatingExtractorDelegate");
        break;
      case MERGE:
        setDelegate("orderedMergingExtractorDelegate");
        break;
    }
  }

  public String getStreamSource() {
    return streamSource;
  }

  public void setStreamSource(String streamSource) {
    this.streamSource = streamSource;
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
