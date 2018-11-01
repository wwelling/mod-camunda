package org.folio.rest.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class OkapiResponse implements Serializable {

  private static final long serialVersionUID = -6503338770047925751L;

  private Integer statusCode;
  private Map<String, String> headers;
  private String body;

  @Override
  public String toString() {
    return "OkapiResponse{" +
      "statusCode=" + statusCode +
      ", headers=" + headers +
      ", body='" + body + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OkapiResponse that = (OkapiResponse) o;
    return Objects.equals(statusCode, that.statusCode) &&
      Objects.equals(headers, that.headers) &&
      Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {

    return Objects.hash(statusCode, headers, body);
  }

//  public OkapiResponse(Integer statusCode, Map<String, String> headers, String body) {
//
//    this.statusCode = statusCode;
//    this.headers = headers;
//    this.body = body;
//  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
