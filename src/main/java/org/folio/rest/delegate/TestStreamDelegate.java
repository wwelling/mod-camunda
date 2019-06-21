package org.folio.rest.delegate;

import static org.camunda.spin.Spin.JSON;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.service.LoginService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TestStreamDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private LoginService loginService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    RestTemplate restTemplate = new RestTemplate();

    String tenant = execution.getTenantId();

    OkapiRequest loginOkapiRequest = new OkapiRequest();
    loginOkapiRequest.setTenant(tenant);
    loginOkapiRequest.setRequestUrl("http://localhost:9130/authn/login");
    loginOkapiRequest.setRequestMethod("POST");
    loginOkapiRequest.setRequestContentType("application/json");
    loginOkapiRequest.setResponseBodyName("loginResponseBody");
    loginOkapiRequest.setResponseHeaderName("loginResponseHeader");
    loginOkapiRequest.setResponseStatusName("loginResponseStatus");

    // A bit redundant, may want to create a login payload model, or eventually handle this better
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", "diku_admin");
    jsonObject.put("password", "admin");

    SpinJsonNode loginJsonNode = JSON(jsonObject.toString());

    loginOkapiRequest.setRequestPayload(loginJsonNode);
    log.info("json: {}", jsonObject.toString());

    FolioLogin newLogin = loginService.folioLogin(loginOkapiRequest);
    newLogin.setUsername("diku_admin");
    log.info("NEW LOGIN: {}", newLogin);

    String okapiToken = newLogin.getxOkapiToken();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_STREAM_JSON);
    headers.add("X-Okapi-Tenant", tenant);
    headers.add("X-Okapi-Token", okapiToken);

    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

    ResponseEntity<Resource> responseEntity = restTemplate.exchange("http://localhost:9130/extractors/{id}/run", HttpMethod.GET, requestEntity, Resource.class, "ed75fb11-abb2-41d9-98f7-aeb79d7700f4");
    
    InputStream is = responseEntity.getBody().getInputStream();

    Stream<String> data = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines();

    streamService.setStream(data);
  }
}
