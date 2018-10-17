package org.folio.rest.delegate;

import org.folio.rest.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOkapiRequestDelegate extends AbstractLoggingDelegate {

  @Autowired
  protected HttpService httpService;

}
