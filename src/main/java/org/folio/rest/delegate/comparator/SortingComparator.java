package org.folio.rest.delegate.comparator;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.folio.rest.workflow.dto.Comparison;

import com.fasterxml.jackson.databind.JsonNode;

public class SortingComparator implements Comparator<JsonNode> {
  private final List<Comparison> enhancementComparisons;

  public SortingComparator(List<Comparison> enhancementComparisons) {
    this.enhancementComparisons = enhancementComparisons;
  }

  @Override
  public int compare(JsonNode o1, JsonNode o2) {
    int result = 0;

    for (Comparison ec : enhancementComparisons) {

      Optional<JsonNode> oo1 = Optional.ofNullable(o1.at(ec.getSourceProperty()));
      Optional<JsonNode> oo2 = Optional.ofNullable(o2.at(ec.getTargetProperty()));

      if (!oo1.isPresent()) {
        return 1;
      }

      if (!oo2.isPresent()) {
        return -1;
      }

      String s1 = oo1.get().asText();
      String s2 = oo2.get().asText();

      if ((result = s1.compareTo(s2)) != 0) {
        break;
      }
    }
    return result;
  }

  public static SortingComparator of(List<Comparison> enhancementComparisons) {
    return new SortingComparator(enhancementComparisons);
  }

}