package org.folio.rest.service;

import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
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

import java.util.HashMap;
import java.util.Map;

import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

@Service
public class OkapiRequestService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final String HEADER_OKAPI_TOKEN = "X-Okapi-Token";

  @Autowired HttpService httpService;

  @Value("${tenant.headerName:X-Okapi-Tenant}")
  private String tenantHeaderName;

    public OkapiResponse okapiRestCall(OkapiRequest okapiRequest) {

    log.info("Executing Okapi Rest Call service");

    String tenant = okapiRequest.getTenant();
    String contentType = okapiRequest.getRequestContentType();
    String url = okapiRequest.getRequestUrl();
    String payload = okapiRequest.getRequestPayload().toString();
    String token = okapiRequest.getOkapiToken();

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
        request = new HttpEntity<>(headers);
        response = httpService.exchange(url, httpMethod, request, String.class, uriVariables);

        int statusCode1 = response.getStatusCodeValue();
        Map<String, String> responseHeaders1 = new HashMap<>();
        responseHeaders1.put("x-okapi-token", response.getHeaders().getFirst("x-okapi-token"));
        responseHeaders1.put("refreshtoken", response.getHeaders().getFirst("refreshtoken"));
        SpinJsonNode responseBody1 = S(response.getBody(), json());

        OkapiResponse okapiResponse1 = new OkapiResponse();
        okapiResponse1.setStatusCode(statusCode1);
        okapiResponse1.setHeaders(responseHeaders1);
        okapiResponse1.setBody(responseBody1);

        return okapiResponse1;

      case POST:
        addContentTypeHeader(headers, contentType);
        addTenantHeader(headers, tenant);
        addOkapiToken(headers, token);
        request = new HttpEntity<>(payload, headers);
        log.info("Request: {}", request);

        response = httpService.exchange(url, httpMethod, request, String.class, uriVariables);

        log.info("<< RESPONSE >>");
        log.info("STATUS: {}", response.getStatusCode().toString());
        log.info("HEADERS: {}", response.getHeaders().toString());
        log.info("BODY: {}", response.getBody().toString());

        int statusCode = response.getStatusCodeValue();
        //String responseBody = response.getBody().toString();
        SpinJsonNode responseBody = S(response.getBody(), json());

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("x-okapi-token", response.getHeaders().getFirst("x-okapi-token"));
        responseHeaders.put("refreshtoken", response.getHeaders().getFirst("refreshtoken"));

        OkapiResponse okapiResponse = new OkapiResponse();
        okapiResponse.setStatusCode(statusCode);
        okapiResponse.setHeaders(responseHeaders);
        okapiResponse.setBody(responseBody);

        return okapiResponse;



      case PUT:
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
}