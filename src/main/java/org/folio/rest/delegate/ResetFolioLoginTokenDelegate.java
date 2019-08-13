package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.model.FolioLogin;
import org.springframework.stereotype.Service;

@Service
public class ResetFolioLoginTokenDelegate extends AbstractRuntimeDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Reset Folio Login Delegate");

    FolioLogin folioLogin = (FolioLogin) execution.getVariable("folioLogin");
    folioLogin.setxOkapiToken("");

    execution.setVariable("folioLogin", folioLogin);
  }
}
