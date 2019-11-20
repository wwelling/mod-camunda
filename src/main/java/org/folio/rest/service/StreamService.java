package org.folio.rest.service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.folio.rest.delegate.comparator.SortingComparator;
import org.folio.rest.delegate.iterable.EnhancingFluxIterable;
import org.folio.rest.workflow.components.EnhancementComparison;
import org.folio.rest.workflow.components.EnhancementMapping;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class StreamService {

  private final ObjectMapper objectMapper;

  private final Map<String, Flux<String>> fluxes;

  private final Map<String, List<String>> reports;

  public StreamService(ObjectMapper objectMapper) {
    this.objectMapper =objectMapper;
    fluxes = new HashMap<String, Flux<String>>();
    reports = new HashMap<String, List<String>>();
  }

  public Flux<String> getFlux(String id) {
    return fluxes.get(id);
  }

  public String concatenateFlux(String firstFluxId, Flux<String> secondFlux) {
    Flux<String> firstFlux = getFlux(firstFluxId);
    return setFlux(firstFluxId, firstFlux.concatWith(secondFlux));
  }

  public String orderedMergeFlux(String firstFluxId, Flux<String> secondFlux, List<EnhancementComparison> enhancementComparisons) throws JsonParseException, JsonMappingException, IOException {
    Flux<String> firstFlux = getFlux(firstFluxId);

    Flux<String> result;
    if (enhancementComparisons.size() > 0) {
      SortingComparator comparator = SortingComparator.of(enhancementComparisons);
      Flux<JsonNode> primary = toJsonNodeFlux(firstFlux);
      Flux<JsonNode> secondary = toJsonNodeFlux(secondFlux);
      result = toStringFlux(primary.mergeOrderedWith(secondary, comparator));
    } else {
      result = Flux.merge(firstFlux, secondFlux);
    }

    return setFlux(firstFluxId, result);
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
  public String enhanceFlux(String primaryFluxId, Flux<String> inFlux, List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings) throws IOException {
    Flux<String> enhancedFlux = enhanceFlux(getFlux(primaryFluxId), inFlux, enhancementComparisons, enhancementMappings);
    return setFlux(primaryFluxId, enhancedFlux);
  }

  public String map(String id, Function<String, String> map) {
    return setFlux(id, getFlux(id).map(map));
  }

  public String map(String id, int buffer, int delay, Function<List<String>, String> map) {
    return setFlux(id, getFlux(id)
      .buffer(buffer)
      .delayElements(
        Duration.ofSeconds(delay),
        Schedulers.single())
      .map(map));
    // return setFlux(id, getFlux(id).buffer(buffer).map(map).map(d -> {
    //   try {
    //     Thread.sleep(delay * 1000);
    //   } catch (InterruptedException e) {
    //     e.printStackTrace();
    //   }
    //   return d;
    // }));
  }

  public String createFlux(Flux<String> flux) {
    String id = UUID.randomUUID().toString();
    fluxes.put(id, flux.doFinally(s -> fluxes.remove(id)));
    return id;
  }

  public String setFlux(String id, Flux<String> flux) {
    fluxes.put(id, flux); //.doFinally(s -> fluxes.remove(id)));
    return id;
  }

  Flux<String> enhanceFlux(Flux<String> primaryFlux, Flux<String> inFlux, List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings) throws IOException {
    Flux<JsonNode> primary = toJsonNodeFlux(primaryFlux);
    Flux<JsonNode> secondary = toJsonNodeFlux(inFlux);
    Flux<JsonNode> result = Flux.fromIterable(EnhancingFluxIterable.of(primary, secondary, enhancementComparisons, enhancementMappings));
    return toStringFlux(result);
  }

  public Flux<JsonNode> toJsonNodeFlux(Flux<String> stringFlux) {
    return stringFlux.map(p -> {
      Optional<JsonNode> node = Optional.empty();
      try {
        node = Optional.ofNullable(objectMapper.readTree(p));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return node;
    }).filter(on -> on.isPresent()).map(on -> on.get());
  }

  public Flux<String> toStringFlux(Flux<JsonNode> jsonNodeFlux) {
    return jsonNodeFlux.map(n -> {
      Optional<String> value = Optional.empty();
      try {
        value = Optional.ofNullable(objectMapper.writeValueAsString(n));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
      return value;
    }).filter(v -> v.isPresent()).map(v -> v.get());
  }

  public void appendToReport(String primaryStreamId, String data) {
    if (reports.containsKey(primaryStreamId)) {
      reports.get(primaryStreamId).add(data);
    } else {
      List<String> reportData = new ArrayList<String>();
      reportData.add(data);
      reports.put(primaryStreamId, reportData);
    }
  }

  public List<String> getReport(String primaryStreamId) {
    return reports.get(primaryStreamId);
  }

  public void clearReport(String primaryStreamId) {
    reports.remove(primaryStreamId);
  }
}
