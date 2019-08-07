package org.folio.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.folio.rest.domain.model.AbstractBaseEntity;
import org.folio.rest.jms.model.TriggerType;
import org.springframework.http.HttpMethod;

@Entity
public class Trigger extends AbstractBaseEntity {

  @NotNull
  @Size(min = 4, max = 64)
  @Column(unique = true)
  private String name;

  @NotNull
  @Size(min = 4, max = 256)
  @Column
  private String description;

  @NotNull
  @Enumerated(EnumType.STRING)
  private TriggerType type;

  @NotNull
  @Enumerated(EnumType.STRING)
  private HttpMethod method;

  @NotNull
  @Column
  private String pathPattern;

  public Trigger() {
    super();
  }

  public Trigger(String name, String description, TriggerType type, HttpMethod method, String pathPattern) {
    this();
    this.name = name;
    this.description = description;
    this.type = type;
    this.method = method;
    this.pathPattern = pathPattern;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TriggerType getType() {
    return type;
  }

  public void setType(TriggerType type) {
    this.type = type;
  }

  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(HttpMethod method) {
    this.method = method;
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public void setPathPattern(String pathPattern) {
    this.pathPattern = pathPattern;
  }

}
