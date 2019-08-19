package org.folio.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.folio.rest.jms.model.TriggerType;
import org.springframework.http.HttpMethod;

@Entity
public class EventTrigger extends Trigger {

  @NotNull
  @Column
  private String pathPattern;

  public EventTrigger() {
    super();
    this.pathPattern = "";
  }

  public EventTrigger(String name, String description, TriggerType type, HttpMethod method) {
    super(name, description, type, method);
    this.pathPattern = "";
  }

  public EventTrigger(String name, String description, TriggerType type, HttpMethod method, String pathPattern) {
    super(name, description, type, method);
    this.pathPattern = pathPattern;
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public void setPathPattern(String pathPattern) {
    this.pathPattern = pathPattern;
  }
}
