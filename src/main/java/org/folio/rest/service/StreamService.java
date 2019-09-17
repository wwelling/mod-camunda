package org.folio.rest.service;

import java.io.IOException;
import java.util.function.Function;

import reactor.core.publisher.Flux;

public interface StreamService<F> {

  public Flux<F> getFlux(String id);

  public String setFlux(Flux<F> flux);

  public String map(String id, Function<F, F> map);

  public String concatenateFlux(String firstFluxId, Flux<F> secondFlux);

  public String orderedMergeFlux(String firstFluxId, Flux<F> secondFlux, String comparisonProperty);

  public String enhanceFlux(String firstFluxId, Flux<F> secondFlux, String comparisonProperty, String enhancementProperty) throws IOException;

}