package org.folio.rest.service;

import static java.util.Comparator.nullsLast;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.folio.rest.delegate.comparator.PropertyComparator;
import org.folio.rest.delegate.iterable.EnhancingFluxIterable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Service
public class StreamService {

  private final ObjectMapper mapper;

  private final Map<String, Flux<String>> fluxes;

  public StreamService(ObjectMapper mapper) {
    this.mapper = mapper;
    fluxes = new HashMap<String, Flux<String>>();
  }

  public Flux<String> getFlux(String id) {
    return fluxes.get(id);
  }

  public String concatenateFlux(String firstFluxId, Flux<String> secondFlux) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    return setFlux(firstFluxId, firstFlux.concatWith(secondFlux));
  }

  public String orderedMergeFlux(String firstFluxId, Flux<String> secondFlux, String comparisonProperties) throws JsonParseException, JsonMappingException, IOException {
    Flux<String> firstFlux = getFlux(firstFluxId);

    @SuppressWarnings("unchecked")
    Map<String, String> comparisonMap = mapper.readValue(comparisonProperties, LinkedHashMap.class);

    Comparator<String> comparator = null;
    AtomicInteger index = new AtomicInteger();
    for (Entry<String, String> entry : comparisonMap.entrySet()) {
      Comparator<String> newComparator = nullsLast(PropertyComparator.of(entry.getKey(), entry.getValue()));
      if (index.getAndIncrement() == 0) {
        comparator = newComparator;
      } else {
        comparator = comparator.thenComparing(newComparator);
      }
    }
    return setFlux(firstFluxId, firstFlux.mergeOrderedWith(secondFlux, comparator));
  }

  /**
   * Compares two fluxes of JSON strings using an ordered map of comparison
   * properties and augments the first flux with an enhancement property from the
   * second flux when there is a match. Primary flux and input flux must be sorted
   * by the comparison properties.
   * 
   * @param primaryFluxId
   * @param inFlux
   * @param comparisonProperties
   * @param enhancementProperty
   * @return primaryFluxId
   * @throws IOException
   */
  public String enhanceFlux(String primaryFluxId, Flux<String> inFlux, String comparisonProperties, String enhancementProperty) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, String> comparisonMap = mapper.readValue(comparisonProperties, LinkedHashMap.class);
    Flux<String> enhancedFlux = enhanceFlux(getFlux(primaryFluxId), inFlux, comparisonMap, enhancementProperty);
    return setFlux(primaryFluxId, enhancedFlux);
  }

  public String setFlux(Flux<String> flux) {
    String id = UUID.randomUUID().toString();
    return setFlux(id, flux);
  }

  public String map(String id, Function<String, String> map) {
    return setFlux(id, getFlux(id).map(map));
  }

  private String setFlux(String id, Flux<String> flux) {
    fluxes.put(id, flux.doFinally(s -> fluxes.remove(id)));
    return id;
  }

  Flux<String> enhanceFlux(Flux<String> primaryFlux, Flux<String> inFlux, Map<String, String> comparisonMap, String enhancementProperty) throws IOException {
    Flux<JsonNode> primary = toJsonNodeFlux(primaryFlux);
    Flux<JsonNode> secondary = toJsonNodeFlux(inFlux);
    Flux<JsonNode> result = Flux.fromIterable(EnhancingFluxIterable.of(primary, secondary, comparisonMap, enhancementProperty));
    return toStringFlux(result);
  }

  Flux<JsonNode> toJsonNodeFlux(Flux<String> stringFlux) {
    return stringFlux.map(p -> {
      Optional<JsonNode> node = Optional.empty();
      try {
        node = Optional.ofNullable(mapper.readTree(p));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return node;
    }).filter(on -> on.isPresent()).map(on -> on.get());
  }

  Flux<String> toStringFlux(Flux<JsonNode> jsonNodeFlux) {
    return jsonNodeFlux.map(n -> {
      Optional<String> value = Optional.empty();
      try {
        value = Optional.ofNullable(mapper.writeValueAsString(n));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
      return value;
    }).filter(v -> v.isPresent()).map(v -> v.get());
  }

}
