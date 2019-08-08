package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class StreamingExtractorTask extends ExtractorTask {

  String streamSource;

  public StreamingExtractorTask(String delegateName) {
    super(delegateName);
    setStreaming(true);
  }

}
