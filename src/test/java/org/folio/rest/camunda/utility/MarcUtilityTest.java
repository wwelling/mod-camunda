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

  /** input */
  static String i(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  /** input */
  static String[] i(String inputPath, String additionalPath) throws IOException {
    return new String[] {
      IOUtils.resourceToString(inputPath, StandardCharsets.UTF_8),
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
    return json.stream().map(n -> om(n)).collect(Collectors.toList());
  }

  /**************************************************************************************
   *
   *
   **************************************************************************************/

  static Stream<Test<String, List<String>>> testSplitRawMarcToMarcJsonRecordsStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>("", List.of()),
        new Test<>(i("/marc4j/54-56-008008027.mrc"), il("/marc4j/54-56-008008027.list-mrc.json"))
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

        for (JsonNode n : oml(data.expected)) {
          ea[0].add(n);
        }

        for (JsonNode n : oml(MarcUtility.splitRawMarcToMarcJsonRecords(data.input))) {
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

  /**************************************************************************************
   *
   *
   **************************************************************************************/

  static Stream<Test<Object, String>> testAddFieldToMarcJsonStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new IllegalArgumentException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/fields/999.mrc.json"), i("/marc4j/withfields/54-56-008008027+999.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testAddFieldToMarcJsonStream")
  void testAddFieldToMarcJson(Test<Object, String> data) {
    String marcJson = data.input != null ? ((String[]) data.input)[0] : null;
    String fieldJson = data.input != null ? ((String[]) data.input)[1] : null;

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.addFieldToMarcJson(marcJson, fieldJson));
    } else {
      try {
        assertEquals(om(data.expected), om(MarcUtility.addFieldToMarcJson(marcJson, fieldJson)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**************************************************************************************
   *
   *
   **************************************************************************************/

   static Stream<Test<String, String>> testUpdateControlNumberFieldStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json")),
        new Test<>(i("/marc4j/54-56-008008027-0-001.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testUpdateControlNumberFieldStream")
  void testUpdateControlNumberField(Test<String, String> data) {
    String marcJson = data.input;
    String controlNumber = "001";

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.updateControlNumberField(marcJson, controlNumber));
    } else {
      try {
        assertEquals(om(data.expected), om(MarcUtility.updateControlNumberField(marcJson, controlNumber)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

}
