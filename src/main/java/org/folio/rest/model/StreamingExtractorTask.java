package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class StreamingExtractorTask extends ExtractorTask {

  String streamSource;

  public StreamingExtractorTask() {
    super();
    setStreaming(true);
  }

  public String getStreamSource() {
    return streamSource;
  }

  public void setStreamSource(String streamSource) {
    this.streamSource = streamSource;
  }

}
