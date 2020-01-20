package org.folio.rest.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContextCacheService {

  private final static Logger logger = LoggerFactory.getLogger(ContextCacheService.class);

  private final static Map<String, Object> CACHE = new ConcurrentHashMap<String, Object>();

  public void put(String key, Object value) {
    logger.info("Set cache {}", key);
    CACHE.put(key, value);
  }

  public Optional<Object> pull(String key) {
    logger.info("Pull cache {}", key);
    return Optional.ofNullable(CACHE.remove(key));
  }

}
