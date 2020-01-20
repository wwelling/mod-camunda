package org.folio.rest.delegate;

import org.folio.rest.service.ContextCacheService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractWorkflowDelegate extends AbstractRuntimeDelegate {

  @Autowired
  protected ContextCacheService contextCacheService;

  public abstract Class<?> fromTask();

}
