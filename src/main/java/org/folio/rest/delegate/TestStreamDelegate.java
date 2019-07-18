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
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  private final WebClient webClient;

  public TestStreamDelegate(WebClient.Builder webClientBuilder) {
    super();
    webClient = webClientBuilder.baseUrl(OKAPI_LOCATION).build();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();
    System.out.println(String.format("%s STARTED", delegateName));

    JsonNode payload = (JsonNode) execution.getVariable("payload");
    String extratorId = payload.get("extractorId").asText();

    String tenant = execution.getTenantId();

    FolioLogin newLogin = login("tern", OKAPI_LOCATION, "tern_admin", "admin");
    log.info("NEW LOGIN: {}", newLogin);
    String token = newLogin.getxOkapiToken();
    execution.setVariable("okapiToken", token);

    System.out.println("START REQUEST");

    streamService.setFlux(
      webClient
        .get()
        .uri(String.format("%s/extractors/{id}/run", OKAPI_LOCATION), extratorId)
        .header("X-Okapi-Tenant", tenant)
        .header("X-Okapi-Token", token)
        .accept(MediaType.APPLICATION_STREAM_JSON)
        .retrieve()
        .bodyToFlux(String.class)
    );

    System.out.println("STREAM DELEGATE FINISHED");
  }

  private FolioLogin login(String tenant, String baseUrl, String username, String password) {
    
    OkapiRequest loginOkapiRequest = new OkapiRequest();
    loginOkapiRequest.setTenant(tenant);
    loginOkapiRequest.setRequestUrl(String.format("%s/authn/login", baseUrl));
    loginOkapiRequest.setRequestMethod("POST");
    loginOkapiRequest.setRequestContentType("application/json");
    loginOkapiRequest.setResponseBodyName("loginResponseBody");
    loginOkapiRequest.setResponseHeaderName("loginResponseHeader");
    loginOkapiRequest.setResponseStatusName("loginResponseStatus");

    // A bit redundant, may want to create a login payload model, or eventually handle this better
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", username);
    jsonObject.put("password", password);

    SpinJsonNode loginJsonNode = JSON(jsonObject.toString());

    loginOkapiRequest.setRequestPayload(loginJsonNode);
    FolioLogin newLogin = loginService.folioLogin(loginOkapiRequest);
    newLogin.setUsername(username);
    return newLogin;
  }
}
