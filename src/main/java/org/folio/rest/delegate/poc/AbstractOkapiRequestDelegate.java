package org.folio.rest.delegate.poc;

import org.folio.spring.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOkapiRequestDelegate extends TestAbstractLoggingDelegate {

  @Autowired
  protected HttpService httpService;

}
