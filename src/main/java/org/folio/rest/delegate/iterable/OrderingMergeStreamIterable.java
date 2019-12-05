package org.folio.rest.delegate.iterable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.folio.rest.delegate.comparator.SortingComparator;
import org.folio.rest.workflow.components.EnhancementComparison;

import com.fasterxml.jackson.databind.JsonNode;

public class OrderingMergeStreamIterable implements Iterable<JsonNode> {

  private final Iterator<JsonNode> primary;

  private final Iterator<JsonNode> input;

  private final SortingComparator sortingComparator;

  public OrderingMergeStreamIterable(Stream<JsonNode> primaryStream, Stream<JsonNode> inStream,
      List<EnhancementComparison> enhancementComparisons) {
    this.primary = primaryStream.iterator();
    this.input = inStream.iterator();
    this.sortingComparator = SortingComparator.of(enhancementComparisons);
  }

  @Override
  public Iterator<JsonNode> iterator() {
    return new Iterator<JsonNode>() {

      private Optional<JsonNode> primaryNode = primary.hasNext() ? Optional.of(primary.next()) : Optional.empty();

      private Optional<JsonNode> inputNode = input.hasNext() ? Optional.of(input.next()) : Optional.empty();

      @Override
      public boolean hasNext() {
        return primary.hasNext() || input.hasNext() || primaryNode.isPresent() || inputNode.isPresent();
      }

      @Override
      public JsonNode next() {

        if (!primaryNode.isPresent() && primary.hasNext()) {
          primaryNode = Optional.of(primary.next());
        }

        if (!inputNode.isPresent() && input.hasNext()) {
          inputNode = Optional.of(input.next());
        }

        if (!inputNode.isPresent()) {
          return primaryNode.get();
        }

        if (!primaryNode.isPresent()) {
          return inputNode.get();
        }

        if (sortingComparator.compare(primaryNode.get(), inputNode.get()) > 0) {
          return inputNode.get();
        }
        return primaryNode.get();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  public Stream<JsonNode> toStream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public static OrderingMergeStreamIterable of(Stream<JsonNode> primaryStream, Stream<JsonNode> inStream,
      List<EnhancementComparison> enhancementComparisons) {
    return new OrderingMergeStreamIterable(primaryStream, inStream, enhancementComparisons);
  }

}
