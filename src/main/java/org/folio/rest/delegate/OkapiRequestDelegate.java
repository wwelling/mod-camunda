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

  private static final String REQUEST_URL = "requestUrl";
  private static final String REQUEST_METHOD = "requestMethod";
  private static final String REQUEST_PAYLOAD = "requestPayload";
  private static final String REQUEST_URI_VARIABLES = "requestUriVariables";

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

    String requestUrl = execution.getVariable(REQUEST_URL).toString();
    String requestMethod = execution.getVariable(REQUEST_METHOD).toString();

    // optional
    Object[] requestUriVariables = execution.getVariable(REQUEST_URI_VARIABLES) != null
        ? (Object[]) execution.getVariable(REQUEST_URI_VARIABLES)
        : new Object[0];

    Object contentType = execution.getVariable(REQUEST_CONTENT_TYPE);

    log.info("Request Tenant: {}, URL: {}, Method: {}", tenant, requestUrl, requestMethod);

    HttpHeaders headers = new HttpHeaders();

    if (contentType != null) {
      headers.setContentType(MediaType.valueOf(contentType.toString()));
    } else {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }

    // TODO: design solution to provide authorization
    // headers.add(HttpHeaders.AUTHORIZATION, "Token");
    // headers.add("X-Okapi-Token", "dummyJwt.eyJzdWIiOiJwZXRlciIsInRlbmFudCI6InRlc3RsaWIifQ==.sig");

    headers.add(tenantHeaderName, tenant);

    HttpMethod httpMethod = HttpMethod.valueOf(requestMethod);

    ResponseEntity<JsonNode> response = null;

    // @formatter:off
    switch (httpMethod) {
    case DELETE: {
      HttpEntity<?> request = new HttpEntity<Object>(headers);
      this.restTemplate.exchange(requestUrl, httpMethod, request, String.class, requestUriVariables);
    } break;
    case GET: {
      HttpEntity<?> request = new HttpEntity<Object>(headers);
      response = this.restTemplate.exchange(requestUrl, httpMethod, request, JsonNode.class, requestUriVariables);
    } break;
    case POST:
    case PUT: {
      Object payload = execution.getVariable(REQUEST_PAYLOAD);
      HttpEntity<?> request = new HttpEntity<Object>(payload, headers);
      response = this.restTemplate.exchange(requestUrl, httpMethod, request, JsonNode.class, requestUriVariables);
    } break;
    case HEAD:
    case OPTIONS:
    case PATCH:
    case TRACE:
    default:
      log.warn("{} is not supported!", httpMethod);
      break;
    }
    // @formatter:on

    if (response != null) {

      execution.setVariable(RESPONSE_STATUS, response.getStatusCode());

      execution.setVariable(RESPONSE_BODY, response.getBody());

    }

  }

}
