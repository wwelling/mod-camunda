package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TestAbstractRuntimeDelegate extends TestAbstractLoggingDelegate {

  @Autowired
  protected RuntimeService runtimeService;

}
