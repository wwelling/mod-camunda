package org.folio.rest.delegate.poc;

import static org.camunda.spin.Spin.JSON;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FolioLoginDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private LoginService loginService;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Folio Login Delegate");

    OkapiRequest okapiRequest = new OkapiRequest();
    okapiRequest.setRequestUrl(String.format("%s/authn/login", OKAPI_LOCATION));
    okapiRequest.setRequestMethod("POST");
    okapiRequest.setRequestContentType("application/json");
    okapiRequest.setResponseBodyName("loginResponseBody");
    okapiRequest.setResponseHeaderName("loginResponseHeader");
    okapiRequest.setResponseStatusName("loginResponseStatus");
    okapiRequest.setTenant("tern_admin");

    // A bit redundant, may want to create a login payload model, or eventually handle this better
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("username", "tern");
    jsonObject.put("password", "admin");

    SpinJsonNode jsonNode = JSON(jsonObject.toString());

    okapiRequest.setRequestPayload(jsonNode);
    log.info("json: {}", jsonObject.toString());

    FolioLogin newLogin = loginService.folioLogin(okapiRequest);
    newLogin.setUsername("tern_admin");
    log.info("NEW LOGIN: {}", newLogin);

    execution.setVariable("folioLogin", newLogin);
  }

}
