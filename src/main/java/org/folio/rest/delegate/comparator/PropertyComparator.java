package org.folio.rest.delegate.comparator;

import java.io.IOException;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PropertyComparator implements Comparator<String> {

  private String firstProperty;
  private String secondProperty;

  public PropertyComparator(String firstProperty, String secondProperty) {
    this.setFirstProperty(firstProperty);
    this.setSecondProperty(secondProperty);
  }

  @Override
  public int compare(String firstString, String secondString) {
    ObjectMapper mapper = new ObjectMapper();
    int result = 0;
    try {
      JsonNode firstNode = mapper.readTree(firstString);
      JsonNode secondNode = mapper.readTree(secondString);
      String firstropertyValue = firstNode.get(this.firstProperty).asText();
      String secondPropertyValue = secondNode.get(this.secondProperty).asText();
      result = firstropertyValue.compareTo(secondPropertyValue);

       // System.out.println("\t" + firstropertyValue + " compareTo " + secondPropertyValue + " = " + result);
    } catch (IOException e) {
      // TODO: Handle exceptions in a better way
      throw new RuntimeException(e.getMessage());
    }
    return result;
  }

  public String getFirstProperty() {
    return firstProperty;
  }

  public void setFirstProperty(String firstProperty) {
    this.firstProperty = firstProperty;
  }

  public String getSecondProperty() {
    return secondProperty;
  }

  public void setSecondProperty(String secondProperty) {
    this.secondProperty = secondProperty;
  }
}
