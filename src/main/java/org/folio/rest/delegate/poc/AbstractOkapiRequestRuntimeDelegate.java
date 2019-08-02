package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOkapiRequestRuntimeDelegate extends AbstractOkapiRequestDelegate {

  @Autowired
  protected RuntimeService runtimeService;

}
