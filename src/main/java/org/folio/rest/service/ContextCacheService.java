package org.folio.rest.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class ContextCacheService {

  private final static Map<String, Object> CACHE = new HashMap<String, Object>();

  public void put(String key, Object value) {
    CACHE.put(key, value);
  }

  public Optional<Object> pull(String key) {
    return Optional.ofNullable(CACHE.remove(key));
  }

}
