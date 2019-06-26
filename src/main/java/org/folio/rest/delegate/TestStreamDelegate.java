package org.folio.rest.delegate;

import static org.camunda.spin.Spin.JSON;

import com.fasterxml.jackson.databind.JsonNode;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.service.LoginService;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Scope("prototype")
public class TestStreamDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private LoginService loginService;

  private final WebClient webClient;

  public TestStreamDelegate(WebClient.Builder webClientBuilder) {
    super();
    webClient = webClientBuilder.baseUrl("http://localhost:9130").build();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();
    System.out.println(String.format("%s STARTED", delegateName));

    JsonNode payload = (JsonNode) execution.getVariable("payload");
    String extratorId = payload.get("extractorId").asText();

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

    String token = newLogin.getxOkapiToken();
    
    execution.setVariable("okapiToken", token);

    System.out.println("START REQUEST");

    streamService.setFlux(
      webClient
        .get()
        .uri("/extractors/{id}/run", extratorId)
        .header("X-Okapi-Tenant", tenant)
        .header("X-Okapi-Token", token)
        .accept(MediaType.APPLICATION_STREAM_JSON)
        .retrieve()
        .bodyToFlux(String.class)
    );

    System.out.println("STREAM DELEGATE FINISHED");
  }
}
