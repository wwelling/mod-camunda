package org.folio.rest.delegate.iterable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.folio.rest.delegate.comparator.SortingComparator;
import org.folio.rest.workflow.components.EnhancementComparison;
import org.folio.rest.workflow.components.EnhancementMapping;

import reactor.core.publisher.Flux;

public class EnhancingFluxIterable implements Iterable<JsonNode> {

  private final Iterator<JsonNode> primary;

  private final Iterator<JsonNode> input;

  private final SortingComparator sortingComparator;

  private final List<EnhancementMapping> enhancementMappings;

  public EnhancingFluxIterable(Flux<JsonNode> primaryFlux, Flux<JsonNode> inFlux, 
    List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings) {
    this.primary = primaryFlux.toIterable().iterator();
    this.input = inFlux.toIterable().iterator();
    this.sortingComparator = SortingComparator.of(enhancementComparisons);
    this.enhancementMappings = enhancementMappings;
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
        if(!inputNode.isPresent()) {
          return -1;
        }
        return sortingComparator.compare(node, inputNode.get());
      }

      private void enhanceNode(ObjectNode node) {
        enhancementMappings.forEach(em->{
          JsonNode propNode = inputNode.get().at(em.getFromProperty());
          node.set(em.getToProperty(), propNode);
        });
      }
    };
  }

  public static EnhancingFluxIterable of(Flux<JsonNode> primaryFlux, Flux<JsonNode> inFlux, 
    List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings) {
    return new EnhancingFluxIterable(primaryFlux, inFlux, enhancementComparisons, enhancementMappings);
  }

}
