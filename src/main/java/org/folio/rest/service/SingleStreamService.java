package org.folio.rest.service;

import static java.util.Comparator.nullsLast;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.folio.rest.delegate.comparator.PropertyComparator;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class SingleStreamService extends AbstractStreamService<String> {

  public String orderedMergeFlux(String firstFluxId, Flux<String> secondFlux, String comparisonProperty) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    Comparator<String> comparator = new PropertyComparator(comparisonProperty);
    return setFlux(firstFluxId, firstFlux.mergeOrderedWith(secondFlux, comparator));
  }

  /*
    * Compares two fluxes of JSON strings using a comparisonProperty and augments
    * the first flux with an enhancement property from the second flux when there
    * is a match.
    */
  public String enhanceFlux(String firstFluxId, Flux<String> secondFlux, String comparisonProperty, String enhancementProperty) throws IOException {
    Flux<String> firstFlux = getFlux(firstFluxId);
    Comparator<String> comparator = nullsLast(new PropertyComparator(comparisonProperty));
    ObjectMapper mapper = new ObjectMapper();
    
    Flux<String> result = Flux.empty();
    Iterator<String> firstIter = firstFlux.toIterable().iterator();
    Iterator<String> secondIter = secondFlux.toIterable().iterator();
    String firstString = firstIter.next();
    String secondString = secondIter.next();

    while(firstString != null || secondString != null) {
      JsonNode firstObject = mapper.readTree(firstString);
      JsonNode secondObject = secondString != null ? mapper.readTree(secondString) : null;
      JsonNode enhancementNode = secondObject != null ? secondObject.get(comparisonProperty) : null;
      if (comparator.compare(firstString, secondString) == 0) {
        result = result.concatWith(Flux.just(mapper.writeValueAsString(((ObjectNode) firstObject).set(enhancementProperty, enhancementNode))));
        firstString = firstIter.hasNext() ? firstIter.next() : null;
        secondString = secondIter.hasNext() ? secondIter.next() : null;
      } else if (comparator.compare(firstString, secondString) < 0) {
        result = result.concatWith(Flux.just(mapper.writeValueAsString(firstObject)));
        firstString = firstIter.hasNext() ? firstIter.next() : null;
      } else {
        secondString = secondIter.hasNext() ? secondIter.next() : null;
      }
    }

    return setFlux(firstFluxId, result);
  }

}