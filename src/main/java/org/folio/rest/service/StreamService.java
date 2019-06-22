package org.folio.rest.service;

import java.util.function.Function;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class StreamService {

  private Flux<String> flux;

  public void map(Function<String, String> map) {
    flux = flux.map(map);
  }

  public Flux<String> getFlux() {
    return flux;
  }

  public void setFlux(Flux<String> flux) {
    this.flux = flux;
  }

}