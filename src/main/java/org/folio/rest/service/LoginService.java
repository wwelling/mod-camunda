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
  private HttpService httpService;

  @Autowired
  private OkapiRequestService okapiRequestService;

  public FolioLogin folioLogin(OkapiRequest okapiRequest) {

    OkapiResponse okapiResponse = okapiRequestService.okapiRestCall(okapiRequest);
    log.info("OKAPI RESPONSE: {}", okapiResponse);


    FolioLogin folioLogin = new FolioLogin("un", "token", "refresh");

    return folioLogin;
  }
}
