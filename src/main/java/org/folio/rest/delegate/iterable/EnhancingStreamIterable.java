package org.folio.rest.delegate.iterable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.folio.rest.delegate.comparator.SortingComparator;
import org.folio.rest.workflow.components.EnhancementComparison;
import org.folio.rest.workflow.components.EnhancementMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EnhancingStreamIterable implements Iterable<JsonNode> {

  private final Iterator<JsonNode> primary;

  private final Iterator<JsonNode> input;

  private final SortingComparator sortingComparator;

  private final List<EnhancementMapping> enhancementMappings;

  public EnhancingStreamIterable(Stream<JsonNode> primaryStream, Stream<JsonNode> inStream,
      List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings) {
    this.primary = primaryStream.iterator();
    this.input = inStream.iterator();
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
        if (!inputNode.isPresent()) {
          return -1;
        }
        return sortingComparator.compare(node, inputNode.get());
      }

      private void enhanceNode(ObjectNode node) {
        enhancementMappings.forEach(em -> {
          if (em.isMultiple()) {
            ArrayNode multiple;
            if (node.has(em.getToProperty())) {
              multiple = (ArrayNode) node.get(em.getToProperty());
            } else {
              multiple = node.putArray(em.getToProperty());
            }
            multiple.add(inputNode.get());
          } else {
            JsonNode propNode = inputNode.get().at(em.getFromProperty());
            node.set(em.getToProperty(), propNode);
          }
        });
      }
    };
  }

  public Stream<JsonNode> toStream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public static EnhancingStreamIterable of(Stream<JsonNode> primaryStream, Stream<JsonNode> inStream,
      List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings) {
    return new EnhancingStreamIterable(primaryStream, inStream, enhancementComparisons, enhancementMappings);
  }

}
