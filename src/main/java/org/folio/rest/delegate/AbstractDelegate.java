package org.folio.rest.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractDelegate implements JavaDelegate {

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
