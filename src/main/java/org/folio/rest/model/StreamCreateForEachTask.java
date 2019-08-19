package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class StreamCreateForEachTask extends Task {

  String endpoint;

  String target;

  String source;

  String uniqueBy;

  public StreamCreateForEachTask() {
    super();
    setDelegate("streamCreateForEachDelegate");
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getUniqueBy() {
    return uniqueBy;
  }

  public void setUniqueBy(String uniqueBy) {
    this.uniqueBy = uniqueBy;
  }

}
