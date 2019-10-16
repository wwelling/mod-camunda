package org.folio.rest.delegate.comparator;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

public class SortingComparator implements Comparator<JsonNode> {
  private final Map<String, String> comparisonMap;

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

      if ((result = s1.compareTo(s2)) != 0) {
        break;
      }
    }
    return result;
  }

  public static SortingComparator of(Map<String, String> comparisonMap) {
    return new SortingComparator(comparisonMap);
  }

}