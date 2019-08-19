package org.folio.rest.delegate.poc;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestAbstractLoggingDelegate implements JavaDelegate {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

}
