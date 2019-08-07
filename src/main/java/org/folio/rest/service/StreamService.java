package org.folio.rest.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class StreamService {

  private final Map<String, Flux<String>> fluxes;

  public StreamService() {
    fluxes = new HashMap<String, Flux<String>>();
  }

  public Flux<String> getFlux(String id) {
    return fluxes.get(id);
  }

  public String concatenateFlux(String firstFluxId, Flux<String> secondFlux) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    return setFlux(firstFlux.concatWith(secondFlux));
  }

  public String setFlux(Flux<String> flux) {
    String id = UUID.randomUUID().toString();
    fluxes.put(id, flux);
    flux.doFinally(f->{
      fluxes.remove(id);
    });
    return id;
  }

}