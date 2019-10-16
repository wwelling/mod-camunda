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

      private Optional<JsonNode> inputNode = nextInput();

      @Override
      public boolean hasNext() {
        return primary.hasNext();
      }

      @Override
      public JsonNode next() {
        JsonNode primaryNode = primary.next();

        int result = compareInput(primaryNode);

        while (result > 0) {
          nextInput();
          result = compareInput(primaryNode);
        }

        if (result == 0) {
          enhanceNode((ObjectNode) primaryNode);
          nextInput();
        }

        return primaryNode;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      private Optional<JsonNode> nextInput() {
        inputNode = input.hasNext() ? Optional.of(input.next()) : Optional.empty();
        return inputNode;
      }

      private int compareInput(JsonNode node) {
        return sortingComparator.compare(node, inputNode.get());
      }

      private void enhanceNode(ObjectNode node) {
        node.set(enhancementProperty, inputNode.get().get(enhancementProperty));
      }

    };
  }

  public static EnhancingFluxIterable of(Flux<JsonNode> primaryFlux, Flux<JsonNode> inFlux, Map<String, String> comparisonMap, String enhancementProperty) {
    return new EnhancingFluxIterable(primaryFlux, inFlux, comparisonMap, enhancementProperty);
  }

}
