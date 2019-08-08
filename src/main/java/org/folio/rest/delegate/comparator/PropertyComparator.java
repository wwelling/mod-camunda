package org.folio.rest.delegate.comparator;

import java.io.IOException;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertyComparator implements Comparator<String> {

  private String property;

  public PropertyComparator(String property) {
    this.setProperty(property);
  }

  @Override
  public int compare(String firstString, String secondString) {
    ObjectMapper mapper = new ObjectMapper();
    int result = 0;
    try {
      JsonNode firstNode = mapper.readTree(firstString);
      JsonNode secondNode = mapper.readTree(secondString);
      String secondPropertyValue = secondNode.get(this.property).asText();
      result = firstNode.get(this.property).asText().compareTo(secondPropertyValue);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }
}