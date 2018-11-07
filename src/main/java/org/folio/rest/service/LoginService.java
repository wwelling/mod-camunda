package org.folio.rest.service;

import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.camunda.spin.Spin.JSON;

@Service
public class LoginService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private OkapiRequestService okapiRequestService;

  public FolioLogin folioLogin(OkapiRequest okapiRequest) {
    log.info("Executing Folio Login Service");

    OkapiResponse okapiResponse = okapiRequestService.okapiRestCall(okapiRequest);
    log.info("OKAPI RESPONSE: {}", okapiResponse);

    String xOkapiToken = okapiResponse.getHeaders().get("x-okapi-token");
    String refreshToken = okapiResponse.getHeaders().get("refreshtoken");


    FolioLogin folioLogin = new FolioLogin("username", xOkapiToken, refreshToken);

    return folioLogin;
  }

  public String refreshToken(OkapiRequest okapiRequest) {
    log.info("Executing Refresh Token Service");

    OkapiResponse okapiResponse = okapiRequestService.okapiRestCall(okapiRequest);
    log.info("OKAPI RESPONSE: {}", okapiResponse);

    SpinJsonNode jsonNode = JSON(okapiResponse.getBody());

    return jsonNode.prop("token").stringValue();
  }
}
