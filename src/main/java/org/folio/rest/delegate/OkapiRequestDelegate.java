package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class OkapiRequestDelegate extends AbstractOkapiRequestDelegate {

  private static final String REQUEST_URL = "url";
  private static final String REQUEST_METHOD = "method";
  private static final String REQUEST_PAYLOAD = "requestPayload";
  private static final String REQUEST_URI_VARIABLES = "uriVariables";

  private static final String REQUEST_CONTENT_TYPE = "requestContentType";

  private static final String RESPONSE_STATUS = "responseStatus";
  private static final String RESPONSE_BODY = "responseBody";

  @Value("tenant.headerName")
  private String tenantHeaderName;

  public OkapiRequestDelegate(RestTemplateBuilder restTemplateBuilder) {
    super(restTemplateBuilder);
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Okapi Request Delegate");

    // required
    String tenant = execution.getTenantId();

    String url = execution.getVariable(REQUEST_URL).toString();
    String method = execution.getVariable(REQUEST_METHOD).toString();

    // optional
    Object[] uriVariables = execution.getVariable(REQUEST_URI_VARIABLES) != null
        ? (Object[]) execution.getVariable(REQUEST_URI_VARIABLES)
        : new Object[0];

    Object contentType = execution.getVariable(REQUEST_CONTENT_TYPE);

    log.info("Request Tenant: {}, URL: {}, Method: {}", tenant, url, method);

    HttpHeaders headers = new HttpHeaders();

    if (contentType != null) {
      headers.setContentType(MediaType.valueOf(contentType.toString()));
    } else {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }

    headers.add(tenantHeaderName, tenant);

    HttpMethod httpMethod = HttpMethod.valueOf(method);

    ResponseEntity<?> response = null;

    switch (httpMethod) {
    case DELETE:
      response = this.restTemplate.exchange(url, httpMethod, new HttpEntity<>(headers), String.class, uriVariables);
      break;
    case GET:
      response = this.restTemplate.exchange(url, httpMethod, new HttpEntity<>(headers), JsonNode.class, uriVariables);
      break;
    case POST:
    case PUT:
      Object payload = execution.getVariable(REQUEST_PAYLOAD);
      HttpEntity<?> request = new HttpEntity<>(payload, headers);
      response = this.restTemplate.exchange(url, httpMethod, request, JsonNode.class, uriVariables);
      break;
    case HEAD:
    case OPTIONS:
    case PATCH:
    case TRACE:
    default:
      log.warn("{} is not supported!", httpMethod);
      break;
    }

    if (response != null) {

      execution.setVariable(RESPONSE_STATUS, response.getStatusCode());

      execution.setVariable(RESPONSE_BODY, response.getBody());

    }

  }

}
