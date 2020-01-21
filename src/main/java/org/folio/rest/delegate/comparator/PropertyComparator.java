package org.folio.rest.delegate.comparator;

import java.io.IOException;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertyComparator implements Comparator<String> {

  private final ObjectMapper mapper;

  private final String firstProperty;

  private final String secondProperty;

  public PropertyComparator(String firstProperty, String secondProperty) {
    this.mapper = new ObjectMapper();
    this.firstProperty = firstProperty;
    this.secondProperty = secondProperty;
  }

  @Override
  public int compare(String firstString, String secondString) {
    int result = 0;
    try {
      JsonNode firstNode = mapper.readTree(firstString);
      JsonNode secondNode = mapper.readTree(secondString);
      String firstropertyValue = firstNode.at(this.firstProperty).asText();
      String secondPropertyValue = secondNode.at(this.secondProperty).asText();
      result = firstropertyValue.compareTo(secondPropertyValue);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    return result;
  }

  public static PropertyComparator of(String firstProperty, String secondProperty) {
    return new PropertyComparator(firstProperty, secondProperty);
  }

}