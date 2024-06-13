package org.folio.rest.camunda.utility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;

class TestUtility {

  private final static ObjectMapper om = new ObjectMapper();

  private TestUtility() {

  }

  /** input */
  static <T> ResponseEntity<T> i(String path, Class<T> valueType) throws IOException {
    return ResponseEntity.ofNullable(om.readValue(new File("src/test/resources/" + path), valueType));
  }

  /** input */
  static String i(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  /** input */
  static String[] i(String path, String additionalPath) throws IOException {
    return new String[] {
      IOUtils.resourceToString(path, StandardCharsets.UTF_8),
      IOUtils.resourceToString(additionalPath, StandardCharsets.UTF_8)
    };
  }

  /** input list */
  static List<String> il(String path) throws IOException {
    String json = i(path);
    List<String> marcjson = new ArrayList<>();

    for (JsonNode n : om.readTree(json)) {
      marcjson.add(n.toString());
    }

    return marcjson;
  }

  /** object map */
  static JsonNode om(String json) {
    try {
      return om.readTree(json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  /** object map list */
  static List<JsonNode> oml(List<String> json) {
    return json.stream().map(n -> om(n)).toList();
  }

}
