package org.folio.rest.delegate.iterable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.folio.rest.delegate.comparator.SortingComparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Flux;

public class EnhancingFluxIterable implements Iterable<JsonNode> {

  private final Iterator<JsonNode> primary;

  private final Iterator<JsonNode> input;

  private final SortingComparator sortingComparator;

  private final String enhancementProperty;

  public EnhancingFluxIterable(Flux<JsonNode> primaryFlux, Flux<JsonNode> inFlux, Map<String, String> comparisonMap, String enhancementProperty) {
    this.primary = primaryFlux.toIterable().iterator();
    this.input = inFlux.toIterable().iterator();
    this.sortingComparator = SortingComparator.of(comparisonMap);
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

        int result = sortingComparator.compare(inputNode.get(), primaryNode);

        while (result < 0) {
          inputNode = input.hasNext() ? Optional.of(input.next()) : Optional.empty();
          result = sortingComparator.compare(inputNode.get(), primaryNode);
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

  public static EnhancingFluxIterable of(Flux<JsonNode> primaryFlux, Flux<JsonNode> inFlux, Map<String, String> comparisonMap, String enhancementProperty) {
    return new EnhancingFluxIterable(primaryFlux, inFlux, comparisonMap, enhancementProperty);
  }

}
