package org.folio.rest.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.URL;

@Entity
public class RestRequestTask extends Task {

  @NotNull
  @URL(protocol = "http", message = "Not a valid URL")
  private String url;

  @NotNull
  @Pattern(regexp = "GET|POST|PUT|DELETE", message = "'httpMethod must be GET, POST, PUT or DELETE'")
  private String httpMethod;

  private String requestBody;

  public RestRequestTask() {
    super();
    this.setDelegate("RestRequestDelegate");
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }

}
