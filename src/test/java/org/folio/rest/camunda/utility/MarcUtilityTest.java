package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarcUtilityTest {

  static ObjectMapper om = new ObjectMapper();

  static class Test<I, O> {
    I input;
    O expected;
    Exception exception;

    Test(I input, O expected) {
      this.input = input;
      this.expected = expected;
    }

    Test(I input, O expected, Exception exception) {
      this.input = input;
      this.expected = expected;
      this.exception = exception;
    }
  }

  static String l(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  static List<String> o(String path) throws IOException {
    String json = l(path);
    List<String> marcjson = new ArrayList<>();

    for (JsonNode n : om.readTree(json)) {
      marcjson.add(n.toString());
    }

    return marcjson;
  }

  static JsonNode om(String json) {
    try {
      return om.readTree(json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  static List<JsonNode> ol(List<String> json) {
    return json.stream().map(n -> om(n)).collect(Collectors.toList());
  }

  static Stream<Test<String, List<String>>> testSplitRawMarcToMarcJsonRecordsStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>("", List.of()),
        new Test<>(l("/marc4j/54-56-008008027.mrc"), o("/marc4j/54-56-008008027.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testSplitRawMarcToMarcJsonRecordsStream")
  void testSplitRawMarcToMarcJsonRecords(Test<String, List<String>> data) {
    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.splitRawMarcToMarcJsonRecords(data.input));
    } else {
      try {

        @SuppressWarnings("unchecked")
        List<JsonNode>[] ea = (List<JsonNode>[]) new List[2];

        ea[0] = new ArrayList<JsonNode>();
        ea[1] = new ArrayList<JsonNode>();

        for (JsonNode n : ol(data.expected)) {
          ea[0].add(n);
        }

        for (JsonNode n : ol(MarcUtility.splitRawMarcToMarcJsonRecords(data.input))) {
          ea[1].add(n);
        }

        for (int i = 0; i < ea[0].size(); i++) {
          assertEquals(ea[0].get(i), ea[1].get(i));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

}
