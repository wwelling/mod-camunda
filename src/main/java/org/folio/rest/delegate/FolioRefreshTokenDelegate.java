package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.camunda.spin.Spin.JSON;

@Service
public class FolioRefreshTokenDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private LoginService loginService;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Folio Refresh Token Delegate");

    OkapiRequest okapiRequest = new OkapiRequest();
    okapiRequest.setRequestUrl(String.format("%s/refresh", OKAPI_LOCATION));
    okapiRequest.setRequestMethod("POST");
    okapiRequest.setRequestContentType("application/json");
    okapiRequest.setResponseBodyName("loginResponseBody");
    okapiRequest.setResponseHeaderName("loginResponseHeader");
    okapiRequest.setResponseStatusName("loginResponseStatus");
    okapiRequest.setTenant("diku");

    FolioLogin login = (FolioLogin) execution.getVariable("folioLogin");
    String refreshToken = login.getRefreshToken();

    // A bit redundant, may want to create a login payload model, or eventually handle this better
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("refreshToken", refreshToken);

    SpinJsonNode jsonNode = JSON(jsonObject.toString());

    okapiRequest.setRequestPayload(jsonNode);
    log.info("json: {}", jsonObject.toString());

    login.setxOkapiToken(loginService.refreshToken(okapiRequest));

    execution.setVariable("folioLogin", login);
  }

}
