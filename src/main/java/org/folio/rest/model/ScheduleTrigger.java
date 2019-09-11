package org.folio.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.folio.rest.jms.model.TriggerType;
import org.springframework.http.HttpMethod;

@Entity
public class ScheduleTrigger extends Trigger {

  @NotNull
  @Column
  private String chronExpression;

  public ScheduleTrigger() {
    super();
    this.chronExpression = "";
  }

  public ScheduleTrigger(String name, String description, TriggerType type, HttpMethod method) {
    super(name, description, type, method);
    this.chronExpression = "";
  }

  public ScheduleTrigger(String name, String description, TriggerType type, HttpMethod method, String chronExpression) {
    super(name, description, type, method);
    this.chronExpression = chronExpression;
  }

  public String getChronExpression() {
    return chronExpression;
  }

  public void setChronExpression(String chronExpression) {
    this.chronExpression = chronExpression;
  }
}
