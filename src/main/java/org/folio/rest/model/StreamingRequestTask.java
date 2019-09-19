package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class StreamingRequestTask extends Task {

    String storageDestination;

    public StreamingRequestTask() {
      super();
      setDelegate("streamingRequestDelegate");
    }

    public String getStorageDestination() {
      return storageDestination;
    }

    public void setStorageDestination(String storageDestination) {
      this.storageDestination = storageDestination;
    }
}