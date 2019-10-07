package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.spring.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RestRequestDelegate extends AbstractRuntimeDelegate {

  private Expression url;

  private Expression httpMethod;

  private Expression requestBody;

  @Value("${tenant.default-tenant}")
  private String DEFAULT_TENANT;

  @Autowired HttpService httpService;


  private static final String NO_VALUE = "NO_VALUE";

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    log.info("STARTING RestRequestDelegate {}", execution.getEventName());

    String urlValue = url.getValue(execution).toString();
    String httpMethodValue = httpMethod.getValue(execution).toString();
    String requestBodyValue = requestBody.getValue(execution).toString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(
      requestBodyValue.equals(NO_VALUE) ? null : requestBodyValue, headers);

    ResponseEntity<String> res = httpService.exchange(
      urlValue,
      HttpMethod.valueOf(httpMethodValue),
      entity, 
      String.class);

    log.info("{}", res.getBody());

  }

  public void setUrl(Expression url) {
    this.url = url;
  }

  public void setHttpMethod(Expression httpMethod) {
    this.httpMethod = httpMethod;
  }

  public void setRequestBody(Expression requestBody) {
    this.requestBody = requestBody;
  }

}