package org.folio.rest.delegate;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractOkapiRequestDelegate extends AbstractLoggingDelegate {

  protected RestTemplate restTemplate;

  public AbstractOkapiRequestDelegate(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

}
