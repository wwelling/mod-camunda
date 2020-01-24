package org.folio.rest.delegate;

import java.util.Optional;

import org.folio.rest.service.ContextCacheService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractWorkflowDelegate extends AbstractRuntimeDelegate {

  @Autowired
  protected ContextCacheService contextCacheService;

  public abstract Class<?> fromTask();

  public void contextCachePut(String key, Object value) {
    contextCacheService.put(key, value);
  }

  public Optional<Object> contextCachePull(String key) {
    return contextCacheService.pull(key);
  }

}
