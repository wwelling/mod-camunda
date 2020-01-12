package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractRuntimeDelegate implements JavaDelegate {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  protected RuntimeService runtimeService;

  @Autowired
  protected ObjectMapper objectMapper;

}
