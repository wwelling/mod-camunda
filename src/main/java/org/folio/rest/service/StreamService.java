package org.folio.rest.service;

import static java.util.Comparator.nullsLast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import org.folio.rest.delegate.comparator.PropertyComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class StreamService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Map<String, Flux<String>> fluxes;

  public StreamService() {
    fluxes = new HashMap<String, Flux<String>>();
  }

  public Flux<String> getFlux(String id) {
    return fluxes.get(id);
  }

  public String concatenateFlux(String firstFluxId, Flux<String> secondFlux) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    return setFlux(firstFluxId, firstFlux.concatWith(secondFlux));
  }

  public String orderedMergeFlux(String firstFluxId, Flux<String> secondFlux, String comparisonProperty) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    Comparator<String> comparator = new PropertyComparator(comparisonProperty);
    return setFlux(firstFluxId, firstFlux.mergeOrderedWith(secondFlux, comparator));
  }

  /*
  * Compares two fluxes of JSON strings using a comparisonProperty and 
  * augments the first flux with an enhancement property from the second
  * flux when there is a match. 
  */
  public String enhanceFlux(String firstFluxId, Flux<String> secondFlux, String comparisonProperty, String enhancementProperty) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    Comparator<String> comparator = nullsLast(new PropertyComparator(comparisonProperty));

    List<String> list1 = new ArrayList<>(Arrays.asList("{\"netid\": 1}","{\"netid\": 3}","{\"netid\": 4}","{\"netid\": 5}", "{\"netid\": 6}", "{\"netid\": 7}", "{\"netid\": 8}"));
    List<String> list2 = new ArrayList<>(Arrays.asList("{\"netid\": 1}", "{\"netid\": 2}", "{\"netid\": 3}", "{\"netid\": 4}", "{\"netid\": 5}", "{\"netid\": 6}", "{\"netid\": 7}"));
    firstFlux = Flux.fromIterable(list1);
    secondFlux = Flux.fromIterable(list2);
    
    Flux<String> result = Flux.empty();
    Iterator<String> firstIter = firstFlux.toIterable().iterator();
    Iterator<String> secondIter = secondFlux.toIterable().iterator();
    String firstString = firstIter.next();
    String secondString = secondIter.next();

    while(firstString != null || secondString != null) {
      if (comparator.compare(firstString, secondString) == 0) {
        log.info("Match: {}, {}", firstString, secondString);
        result = result.concatWith(Flux.fromStream(Stream.of(firstString + ", " + secondString)));
        firstString = firstIter.hasNext() ? firstIter.next() : null;
        secondString = secondIter.hasNext() ? secondIter.next() : null;
      } else if (comparator.compare(firstString, secondString) < 0) {
        log.info("No Match on second flux: {}", firstString);
        result = result.concatWith(Flux.fromStream(Stream.of(firstString)));
        firstString = firstIter.hasNext() ? firstIter.next() : null;
      } else {
        log.info("No Match on first flux: {}", secondString);
        secondString = secondIter.hasNext() ? secondIter.next() : null;
      }
    }

    result.subscribe(item -> System.out.println(item));

    return null;
  }

  private String setFlux(String id, Flux<String> flux) {
    fluxes.put(id, flux.doFinally(s->fluxes.remove(id)));
    return id;
  }

  public String setFlux(Flux<String> flux) {
    String id = UUID.randomUUID().toString();
    return setFlux(id, flux);
  }

  public String map(String id, Function<String, String> map) {
    return setFlux(id, getFlux(id).map(map));
  }

}