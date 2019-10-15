package org.folio.rest.service;

import static java.util.Comparator.nullsLast;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.folio.rest.delegate.comparator.PropertyComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Flux;

@Service
public class StreamService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

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
      Comparator<String> newComparator = nullsLast(new PropertyComparator(entry.getKey(), entry.getValue()));
      if (index.getAndIncrement() == 0) {
        comparator = newComparator;
      } else {
        comparator = comparator.thenComparing(newComparator);
      }
    }
    return setFlux(firstFluxId, firstFlux.mergeOrderedWith(secondFlux, comparator));
  }

  /*
   * Compares two fluxes of JSON strings using an ordered map of comparison
   * properties and augments the first flux with an enhancement property from the
   * second flux when there is a match.
   */
  public String enhanceFlux(String firstFluxId, Flux<String> secondFlux, String comparisonProperties, String enhancementProperty) throws IOException {

    @SuppressWarnings("unchecked")
    Map<String, String> comparisonMap = mapper.readValue(comparisonProperties, LinkedHashMap.class);

    Flux<String> firstFlux = getFlux(firstFluxId);
    Flux<String> result = Flux.empty();
    Iterator<String> firstIter = firstFlux.toIterable().iterator();
    Iterator<String> secondIter = secondFlux.toIterable().iterator();
    String firstString = firstIter.next();
    String secondString = secondIter.next();

    while (firstString != null || secondString != null) {
      JsonNode firstObject = mapper.readTree(firstString);
      JsonNode secondObject = secondString != null ? mapper.readTree(secondString) : null;
      JsonNode enhancementNode = secondObject != null ? secondObject.get(enhancementProperty) : null;
      boolean matched = true;
      for (Entry<String, String> entry : comparisonMap.entrySet()) {
        Comparator<String> comparator = nullsLast(new PropertyComparator(entry.getKey(), entry.getValue()));
        if (comparator.compare(firstString, secondString) < 0) {
          result = result.concatWith(Flux.just(firstString));
          firstString = firstIter.hasNext() ? firstIter.next() : null;
          matched = false;
          break;
        } else if (comparator.compare(firstString, secondString) > 0) {
          secondString = secondIter.hasNext() ? secondIter.next() : null;
          matched = false;
          break;
        }
      }
      if (matched) {
        result = result.concatWith(Flux.just(mapper.writeValueAsString(((ObjectNode) firstObject).set(enhancementProperty, enhancementNode))));
        firstString = firstIter.hasNext() ? firstIter.next() : null;
        secondString = secondIter.hasNext() ? secondIter.next() : null;
      }
    }
    return setFlux(firstFluxId, result);
  }

  public String enhanceFlux2(String firstFluxId, Flux<String> secondFlux, String comparisonProperties, String enhancementProperty) throws IOException {

    @SuppressWarnings("unchecked")
    Map<String, String> comparisonMap = mapper.readValue(comparisonProperties, LinkedHashMap.class);

    Flux<JsonNode> primary = toJsonNodeFlux(getFlux(firstFluxId));
    Flux<JsonNode> secondary = toJsonNodeFlux(secondFlux);
    Flux<JsonNode> sresult = Flux.fromIterable(new EnhancingFluxIterable(primary, secondary, comparisonMap, enhancementProperty));
    return setFlux(firstFluxId, toStringFlux(sresult));
  }

  String setFlux(String id, Flux<String> flux) {
    fluxes.put(id, flux.doFinally(s -> fluxes.remove(id)));
    return id;
  }

  public String setFlux(Flux<String> flux) {
    String id = UUID.randomUUID().toString();
    return setFlux(id, flux);
  }

  public String map(String id, Function<String, String> map) {
    return setFlux(id, getFlux(id).map(map));
  }

  private Flux<JsonNode> toJsonNodeFlux(Flux<String> stringFlux) {
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

  private Flux<String> toStringFlux(Flux<JsonNode> jsonNodeFlux) {
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

  public class EnhancingFluxIterable implements Iterable<JsonNode> {

    private final Iterator<JsonNode> primary;

    private final Iterator<JsonNode> input;

    private final SortingComparator sortCompare;

    private final String enhancementProperty;

    public EnhancingFluxIterable(Flux<JsonNode> primaryFlux, Flux<JsonNode> inFlux, Map<String, String> comparisonMap,
        String enhancementProperty) {
      this.primary = primaryFlux.toIterable().iterator();
      this.input = inFlux.toIterable().iterator();
      this.sortCompare = new SortingComparator(comparisonMap);
      this.enhancementProperty = enhancementProperty;
    }

    @Override
    public Iterator<JsonNode> iterator() {
      return new Iterator<JsonNode>() {

        private Optional<JsonNode> inputNode = input.hasNext() ? Optional.of(input.next()) : Optional.empty();

        @Override
        public boolean hasNext() {
          return primary.hasNext();
        }

        @Override
        public JsonNode next() {
          JsonNode primaryNode = primary.next();

          int result = sortCompare.compare(inputNode.get(), primaryNode);

          while (result < 0) {
            inputNode = input.hasNext() ? Optional.of(input.next()) : Optional.empty();
            result = sortCompare.compare(inputNode.get(), primaryNode);
          }

          if (result == 0) {
            ((ObjectNode) primaryNode).set(enhancementProperty, inputNode.get().get(enhancementProperty));
            inputNode = input.hasNext() ? Optional.of(input.next()) : Optional.empty();
          }

          return primaryNode;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

      };
    }

    public class SortingComparator implements Comparator<JsonNode> {
      private Map<String, String> comparisonMap;

      public SortingComparator(Map<String, String> comparisonMap) {
        this.comparisonMap = comparisonMap;
      }

      @Override
      public int compare(JsonNode o1, JsonNode o2) {
        int result = 0;
        for (Entry<String, String> entry : comparisonMap.entrySet()) {
          Optional<JsonNode> oo1 = Optional.ofNullable(o1.get(entry.getKey()));
          Optional<JsonNode> oo2 = Optional.ofNullable(o2.get(entry.getValue()));

          if (!oo1.isPresent()) {
            return 1;
          }

          if (!oo2.isPresent()) {
            return -1;
          }

          String s1 = oo1.get().asText();
          String s2 = oo2.get().asText();

          // System.out.println("\t" + s1 + " compareTo " + s2 + " = " + s1.compareTo(s2));
          if ((result = s1.compareTo(s2)) != 0) {
            break;
          }
        }
        return result;
      }

    }

  }

}
