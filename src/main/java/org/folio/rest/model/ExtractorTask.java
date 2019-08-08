package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class ExtractorTask extends Task {

  String streamSource;

  public ExtractorTask() {
    super();
    setDelegate("testStreamDelegate");
  }

  public String getStreamSource() {
    return streamSource;
  }

  public void setStreamSource(String streamSource) {
    this.streamSource = streamSource;
  }

}
