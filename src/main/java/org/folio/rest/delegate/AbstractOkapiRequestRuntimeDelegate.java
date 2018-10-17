package org.folio.rest.delegate;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;

public abstract class AbstractOkapiRequestRuntimeDelegate extends AbstractOkapiRequestDelegate {

  @Autowired
  protected RuntimeService runtimeService;

  public AbstractOkapiRequestRuntimeDelegate(RestTemplateBuilder restTemplateBuilder) {
    super(restTemplateBuilder);
  }

}
