package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractDelegate implements JavaDelegate {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  protected ObjectMapper objectMapper;

  public String getExpression() {
    String simpleName = getClass().getSimpleName();
    String delegateName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    return String.format("${%s}", delegateName);
  }

  public Logger getLogger() {
    return logger;
  }

}
