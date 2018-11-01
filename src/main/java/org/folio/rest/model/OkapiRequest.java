package org.folio.rest.model;

import org.camunda.bpm.engine.impl.util.json.JSONObject;

import java.io.Serializable;

public class OkapiRequest implements Serializable {

  private static final long serialVersionUID = 698415949379750160L;
  private String url;
  private String requestMethod;
  private String requestContentType;
  private JSONObject payload;
  private Object[] requestUriVariables;
  private String tenant;
  private String okapiToken;
  private String responseStatusName;
  private String responseHeaderName;
  private String responseBodyName;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getRequestMethod() {
    return requestMethod;
  }

  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  public String getRequestContentType() {
    return requestContentType;
  }

  public void setRequestContentType(String requestContentType) {
    this.requestContentType = requestContentType;
  }

  public JSONObject getPayload() {
    return payload;
  }

  public void setPayload(JSONObject payload) {
    this.payload = payload;
  }

  public Object[] getRequestUriVariables() {
    return requestUriVariables;
  }

  public void setRequestUriVariables(Object[] requestUriVariables) {
    this.requestUriVariables = requestUriVariables;
  }

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public String getOkapiToken() {
    return okapiToken;
  }

  public void setOkapiToken(String okapiToken) {
    this.okapiToken = okapiToken;
  }

  public String getResponseStatusName() {
    return responseStatusName;
  }

  public void setResponseStatusName(String responseStatusName) {
    this.responseStatusName = responseStatusName;
  }

  public String getResponseHeaderName() {
    return responseHeaderName;
  }

  public void setResponseHeaderName(String responseHeaderName) {
    this.responseHeaderName = responseHeaderName;
  }

  public String getResponseBodyName() {
    return responseBodyName;
  }

  public void setResponseBodyName(String responseBodyName) {
    this.responseBodyName = responseBodyName;
  }

}
