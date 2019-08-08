package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class ExtractorTask extends Task {

  private String streamSource;

  private String predicateProperty;

  private MergeStrategy mergeStrategy;

  public ExtractorTask(String delegateName) {
    super();
    setDelegate("testStreamDelegate");
  }

  public ExtractorTask(String delegateName, String predicateProperty, MergeStrategy mergeStrategy) {
    this(delegateName);
    this.predicateProperty = predicateProperty;
    this.mergeStrategy = mergeStrategy;
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
