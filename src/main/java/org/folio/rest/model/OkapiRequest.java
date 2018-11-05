package org.folio.rest.model;

import org.camunda.spin.json.SpinJsonNode;

import java.io.Serializable;

public class OkapiRequest implements Serializable {

  private static final long serialVersionUID = 698415949379750160L;
  private String requestUrl;
  private String requestMethod;
  private String requestContentType;
  private SpinJsonNode requestPayload;
  private Object[] requestUriVariables;
  private String tenant;
  private String okapiToken;
  private String responseStatusName;
  private String responseHeaderName;
  private String responseBodyName;

  public String getRequestUrl() {
    return requestUrl;
  }

  public void setRequestUrl(String requestUrl) {
    this.requestUrl = requestUrl;
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

  public SpinJsonNode getRequestPayload() {
    return requestPayload;
  }

  public void setRequestPayload(SpinJsonNode requestPayload) {
    this.requestPayload = requestPayload;
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
