package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLoggingDelegate implements JavaDelegate {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

}
