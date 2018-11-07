package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.OkapiResponse;
import org.springframework.stereotype.Service;

import static org.camunda.spin.Spin.JSON;

@Service
public class ParseOkapiResponseDelegate extends AbstractRuntimeDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Parse Okapi Response Delegate");

    OkapiResponse okapiResponse = (OkapiResponse) execution.getVariable("okapiResponse");

    SpinJsonNode jsonNodeBody = JSON(okapiResponse.getBody());
    log.info("JSON NODE: {}", jsonNodeBody);
  }
}
