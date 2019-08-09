package org.folio.rest.model;

import javax.persistence.Entity;

@Entity
public class LoginTask extends Task {

  public LoginTask()  {
    super();
    this.setDelegate("folioLoginDelegate");
  }

  public LoginTask(String name) {
    this();
    setName(name);
  }

}
