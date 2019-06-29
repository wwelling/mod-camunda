package org.folio.rest.delegate;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import static org.camunda.spin.Spin.JSON;

@Service
@Scope("prototype")
public class TestAccumulatorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private LoginService loginService;

  private final WebClient webClient;

  public TestAccumulatorDelegate(WebClient.Builder webClientBuilder) {
    super();
    webClient = webClientBuilder.baseUrl("https://folio-okapisnapshot.library.tamu.edu").build();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    OkapiRequest loginOkapiRequest = new OkapiRequest();
    loginOkapiRequest.setTenant("tern");
    loginOkapiRequest.setRequestUrl("https://folio-okapisnapshot.library.tamu.edu/authn/login");
    loginOkapiRequest.setRequestMethod("POST");
    loginOkapiRequest.setRequestContentType("application/json");
    loginOkapiRequest.setResponseBodyName("loginResponseBody");
    loginOkapiRequest.setResponseHeaderName("loginResponseHeader");
    loginOkapiRequest.setResponseStatusName("loginResponseStatus");

    // A bit redundant, may want to create a login payload model, or eventually
    // handle this better
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", "tern_admin");
    jsonObject.put("password", "admin");

    SpinJsonNode loginJsonNode = JSON(jsonObject.toString());

    loginOkapiRequest.setRequestPayload(loginJsonNode);
    log.info("json: {}", jsonObject.toString());

    FolioLogin newLogin = loginService.folioLogin(loginOkapiRequest);
    newLogin.setUsername("tern_admin");
    log.info("NEW LOGIN: {}", newLogin);

    String token = newLogin.getxOkapiToken();

    // String token = (String) execution.getVariable("okapiToken");
    System.out.println(String.format("%s STARTED", delegateName));

    streamService.getFlux().buffer(2).subscribe(rows -> {
      rows.forEach(row -> {

        try {
          Object json = mapper.readValue(row, Object.class);
          String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
          System.out.println(String.format("\n%s: \n", delegateName));
          System.out.println(jsonString);
        } catch (IOException e) {
          e.printStackTrace();
        }

        // try {
        //   webClient
        //     .post()
        //     .uri("/organizations-storage/organizations")
        //     .syncBody(mapper.readTree(row))
        //     .header("X-Okapi-Tenant", "tern")
        //     .header("X-Okapi-Token", token)
        //     .accept(MediaType.APPLICATION_JSON)
        //     .retrieve().bodyToFlux(String.class).subscribe(res -> {
        //       //System.out.println(res);
        //     });
        // } catch (IOException e) {
        //   
        //   e.printStackTrace();
        // }

      });
      System.out.println(String.format("\n%s: %s", delegateName, rows.size()));
    });
  }

}
