package org.folio.rest.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.folio.spring.service.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@Service
public class OkapiRequestService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String HEADER_OKAPI_TOKEN = "X-Okapi-Token";

  @Autowired HttpService httpService;

  @Value("${tenant.headerName:X-Okapi-Tenant}")
  private String tenantHeaderName;

  public OkapiResponse okapiRestCall(OkapiRequest okapiRequest) {

    log.debug("Executing Okapi Rest Call service");

    String tenant = okapiRequest.getTenant();
    String contentType = okapiRequest.getRequestContentType();
    String url = okapiRequest.getRequestUrl();
    String token = okapiRequest.getOkapiToken();
    String payload = okapiRequest.getRequestPayload().toString();

    // optional
    Object[] uriVariables = okapiRequest.getRequestUriVariables() != null
      ? (Object[]) okapiRequest.getRequestUriVariables()
      : new Object[0];

    HttpMethod httpMethod = HttpMethod.valueOf(okapiRequest.getRequestMethod());

    HttpHeaders headers = new HttpHeaders();

    HttpEntity<?> request = null;

    ResponseEntity<?> response = null;

    switch (httpMethod) {
      case DELETE:
      case GET:
        addContentTypeHeader(headers, contentType);
        addTenantHeader(headers, tenant);
        addOkapiToken(headers, token);
        request = new HttpEntity<>(headers);
        log.debug("GET Request: {} {}", url, request);
        response = httpService.exchange(url, httpMethod, request, String.class, uriVariables);
        return mapOkapiResponse(response);

      case POST:
        
        addContentTypeHeader(headers, contentType);
        addTenantHeader(headers, tenant);
        addOkapiToken(headers, token);
        request = new HttpEntity<>(payload, headers);
        log.debug("POST Request: {} {} {}", request, url, uriVariables);

        try {
          response = httpService.exchange(url, httpMethod, request, String.class, uriVariables);
        } catch(HttpClientErrorException httpError) {
          throw new BpmnError("LOGIN_ERROR", String.format("Error logging in, retrying: %s", request.getBody()));
        }

        return mapOkapiResponse(response);

      case PUT:
        payload = okapiRequest.getRequestPayload().toString();
        addContentTypeHeader(headers, contentType);
        addTenantHeader(headers, tenant);
        addOkapiToken(headers, token);
        request = new HttpEntity<>(payload, headers);
        log.debug("PUT Request: {}", request);

        try {
          response = httpService.exchange(url, httpMethod, request, String.class, uriVariables);
        } catch(HttpClientErrorException httpError) {
          throw new BpmnError("LOGIN_ERROR", String.format("Error logging in, retrying: %s", response));
        }

        return mapOkapiResponse(response);
      case HEAD:
      case OPTIONS:
      case PATCH:
      case TRACE:
      default:
        log.warn("{} is not supported!", httpMethod);
        break;
    }
    return new OkapiResponse();
  }

  private void addContentTypeHeader(HttpHeaders headers, Object contentType) {
    if (contentType != null) {
      headers.setContentType(MediaType.valueOf(contentType.toString()));
    } else {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
  }

  private void addTenantHeader(HttpHeaders headers, String tenant) {
    headers.add(tenantHeaderName, tenant);
  }

  private void addOkapiToken(HttpHeaders headers, String token) { headers.add(HEADER_OKAPI_TOKEN, token); }

  private OkapiResponse mapOkapiResponse(ResponseEntity<?> response) {
    log.debug("<< RESPONSE >>");
    log.debug("STATUS: {}", response.getStatusCode().toString());
    log.debug("HEADERS: {}", response.getHeaders().toString());
    if (response.getBody() != null) {
      log.debug("BODY: {}", response.getBody().toString());
    }

    int statusCode = response.getStatusCodeValue();
    String responseBody = "";
    if (response.getBody() != null) {
      responseBody = response.getBody().toString();
    }
    log.debug("responseBody: {}", responseBody);

    Map<String, String> responseHeaders = new HashMap<>();
    responseHeaders.put("x-okapi-token", response.getHeaders().getFirst("x-okapi-token"));
    responseHeaders.put("refreshtoken", response.getHeaders().getFirst("refreshtoken"));

    OkapiResponse okapiResponse = new OkapiResponse();
    okapiResponse.setStatusCode(statusCode);
    okapiResponse.setHeaders(responseHeaders);
    okapiResponse.setBody(responseBody);

    okapiResponse.setResponse(response);

    return okapiResponse;
  }
}
