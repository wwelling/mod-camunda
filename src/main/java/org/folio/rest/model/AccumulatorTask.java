package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class AccumulatorTask extends Task {

  Long accumulateTo;

  Long delayDuration;

  public AccumulatorTask()  {
    super();
    this.setDelegate("streamAccumulationDelegate");
  }

  public AccumulatorTask(String name) {
    this();
    setName(name);
  }

  public Long getAccumulateTo() {
    return accumulateTo;
  }

  public void setAccumulateTo(Long accumulateTo) {
    this.accumulateTo = accumulateTo;
  }

  public Long getDelayDuration() {
    return delayDuration;
  }

  public void setDelayDuration(Long delayDuration) {
    this.delayDuration = delayDuration;
  }

}
