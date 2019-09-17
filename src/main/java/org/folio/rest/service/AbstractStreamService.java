package org.folio.rest.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

public abstract class AbstractStreamService<F> implements StreamService<F> {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Map<String, Flux<F>> fluxes;

  public AbstractStreamService() {
    fluxes = new HashMap<String, Flux<F>>();
  }

  public String concatenateFlux(String firstFluxId, Flux<F> secondFlux) {
    Flux<F> firstFlux = getFlux(firstFluxId);
    return setFlux(firstFluxId, firstFlux.concatWith(secondFlux));
  }

  public Flux<F> getFlux(String id) {
    return fluxes.get(id);
  }

  protected String setFlux(String id, Flux<F> flux) {
    fluxes.put(id, flux.doFinally(s->fluxes.remove(id)));
    return id;
  }

  public String setFlux(Flux<F> flux) {
    String id = UUID.randomUUID().toString();
    return setFlux(id, flux);
  }

  public String map(String id, Function<F, F> map) {
    return setFlux(id, getFlux(id).map(map));
  }

}