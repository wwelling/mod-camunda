package org.folio.rest.service;

import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
