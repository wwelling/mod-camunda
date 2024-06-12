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
import com.fasterxml.jackson.databind.JsonMappingException;
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
   * splitRawMarcToMarcJsonRecords                                                      *
   *************************************************************************************/

  static Stream<Test<String, List<String>>> testSplitRawMarcToMarcJsonRecordsStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>("", List.of()),
        new Test<>(i("/marc4j/54-56-008008027.mrc"), il("/marc4j/54-56-008008027.mrc.json"))
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
   * addFieldToMarcJson                                                                 *
   *************************************************************************************/

  static Stream<Test<Object, String>> testAddFieldToMarcJsonStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new IllegalArgumentException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/field/999.mrc.json"), i("/marc4j/withfields/54-56-008008027+999.mrc.json"))
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
   * updateControlNumberField                                                           *
   *************************************************************************************/

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

  /**************************************************************************************
   * marcJsonToRawMarc                                                                  *
   *************************************************************************************/

  static Stream<Test<String, String>> testMarcJsonToRawMarcStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/54-56-008008027-0.mrc"))
      );
  }

  @ParameterizedTest
  @MethodSource("testMarcJsonToRawMarcStream")
  void testMarcJsonToRawMarc(Test<String, String> data) {
    String marcJson = data.input;

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.marcJsonToRawMarc(marcJson));
    } else {
      try {
        assertEquals(data.expected.trim(), MarcUtility.marcJsonToRawMarc(marcJson).trim());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**************************************************************************************
   * rawMarcToMarcJson                                                                  *
   *************************************************************************************/

  static Stream<Test<String, String>> testRawMarcToMarcJsonStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc"), i("/marc4j/54-56-008008027-0.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testRawMarcToMarcJsonStream")
  void testRawMarcToMarcJson(Test<String, String> data) {
    String rawMarc = data.input;

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.rawMarcToMarcJson(rawMarc));
    } else {
      try {
        assertEquals(om(data.expected), om(MarcUtility.rawMarcToMarcJson(rawMarc)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**************************************************************************************
   * getFieldsFromRawMarc
   *
   **************************************************************************************/

   static Stream<Test<Object, String>> testGetFieldsFromRawMarcStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 245.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromRawMarcStream")
  void testGetFieldsFromRawMarc(Test<Object, String> data) throws JsonProcessingException {
    String rawMarc, tagsJson;

    String[] tags;

    if (data.input == null) {
      rawMarc = null;
      tags = new String[0];
    } else {
      rawMarc = ((String[]) data.input)[0];
      tagsJson = ((String[]) data.input)[1];

      List<String> list = new ArrayList<>();
      for (JsonNode n : om.readTree(tagsJson)) {
        list.add(n.toString());
      }

      tags = om.readValue(tagsJson, String[].class);
    }

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.getFieldsFromRawMarc(rawMarc, tags));
    } else {
      try {
        assertEquals(om(data.expected), om(MarcUtility.getFieldsFromRawMarc(rawMarc, tags).trim()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**************************************************************************************
   * getFieldsFromMarcJson
   *
   **************************************************************************************/

   static Stream<Test<Object, String>> testGetFieldsFromMarcJsonStream() throws IOException {
    return Stream.of(
        new Test<>(null, null, new NullPointerException()),
        new Test<>(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 245.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromMarcJsonStream")
  void testGetFieldsFromMarcJson(Test<Object, String> data) throws JsonProcessingException {
    String marcJson, tagsJson;

    String[] tags;

    if (data.input == null) {
      marcJson = null;
      tags = new String[0];
    } else {
      marcJson = ((String[]) data.input)[0];
      tagsJson = ((String[]) data.input)[1];

      List<String> list = new ArrayList<>();
      for (JsonNode n : om.readTree(tagsJson)) {
        list.add(n.toString());
      }

      tags = om.readValue(tagsJson, String[].class);
    }

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MarcUtility.getFieldsFromMarcJson(marcJson, tags));
    } else {
      try {
        assertEquals(om(data.expected), om(MarcUtility.getFieldsFromMarcJson(marcJson, tags).trim()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

}
