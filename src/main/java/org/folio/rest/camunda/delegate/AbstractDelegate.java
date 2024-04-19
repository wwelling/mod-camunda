package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDelegate implements JavaDelegate {

  private final Logger log;

  AbstractDelegate() {
    // The logger is non-static to ensure that the implementing class name is used for the logger.
    log = LoggerFactory.getLogger(this.getClass());
  }

  @Autowired
  protected ObjectMapper objectMapper;

  public String getExpression() {
    String simpleName = getClass().getSimpleName();
    String delegateName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    return String.format("${%s}", delegateName);
  }

  public Logger getLogger() {
    return log;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
