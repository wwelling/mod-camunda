package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.marc4j.MarcException;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarcUtilityTest {

  static class T<I, O> {
    I input;
    O expected;
    Exception exception;

    T(I input, O expected) {
      this.input = input;
      this.expected = expected;
    }

    T(I input, O expected, Exception exception) {
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

    for (JsonNode n : MarcUtility.mapper.readTree(json)) {
      marcjson.add(n.toString());
    }

    return marcjson;
  }

  /** object map */
  static JsonNode om(String json) {
    try {
      return MarcUtility.mapper.readTree(json);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  /** object map list */
  static List<JsonNode> oml(List<String> json) {
    return json.stream().map(n -> om(n)).toList();
  }

  /**************************************************************************************
   * splitRawMarcToMarcJsonRecords                                                      *
   *************************************************************************************/

  static Stream<T<String, List<String>>> testSplitRawMarcToMarcJsonRecordsStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new NullPointerException()),
        new T<>("", List.of()),
        new T<>(i("/marc4j/54-56-008008027.mrc"), il("/marc4j/54-56-008008027.mrc.json"))
      );
  }

  @Test
  void testDeserializingSubfield() throws JsonProcessingException {
    MarcFactory factory = MarcFactory.newInstance();
    Subfield subfield = factory.newSubfield();
    subfield.setCode("245".charAt(0));
    subfield.setData("data");
    String serialized = MarcUtility.mapper.writeValueAsString(subfield);
    Subfield deserializedSubfield = MarcUtility.mapper.readValue(serialized, Subfield.class);
    assertEquals(subfield.getId(), deserializedSubfield.getId());
    assertEquals(subfield.getCode(), deserializedSubfield.getCode());
    assertEquals(subfield.getData(), deserializedSubfield.getData());
  }

  @ParameterizedTest
  @MethodSource("testSplitRawMarcToMarcJsonRecordsStream")
  void testSplitRawMarcToMarcJsonRecords(T<String, List<String>> data) {
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

  static Stream<T<Object, String>> testAddFieldToMarcJsonStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new IllegalArgumentException()),
        new T<>(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/field/999.mrc.json"), i("/marc4j/withfields/54-56-008008027+999.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testAddFieldToMarcJsonStream")
  void testAddFieldToMarcJson(T<Object, String> data) {
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

  static Stream<T<String, String>> testUpdateControlNumberFieldStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new NullPointerException()),
        new T<>(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json")),
        new T<>(i("/marc4j/54-56-008008027-0-001.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testUpdateControlNumberFieldStream")
  void testUpdateControlNumberField(T<String, String> data) {
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

  static Stream<T<String, String>> testMarcJsonToRawMarcStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new NullPointerException()),
        new T<>("", null, new MarcException()),
        new T<>(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/54-56-008008027-0.mrc"))
      );
  }

  @ParameterizedTest
  @MethodSource("testMarcJsonToRawMarcStream")
  void testMarcJsonToRawMarc(T<String, String> data) {
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

  static Stream<T<String, String>> testRawMarcToMarcJsonStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new NullPointerException()),
        new T<>("", null, new MarcException()),
        new T<>(i("/marc4j/54-56-008008027-0.mrc"), i("/marc4j/54-56-008008027-0.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testRawMarcToMarcJsonStream")
  void testRawMarcToMarcJson(T<String, String> data) {
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

   static Stream<T<Object, String>> testGetFieldsFromRawMarcStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new NullPointerException()),
        new T<>(new String[] { "", "[]" }, null, new MarcException()),
        new T<>(i("/marc4j/54-56-008008027-0.mrc", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 050090245947980.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromRawMarcStream")
  void testGetFieldsFromRawMarc(T<Object, String> data) throws JsonProcessingException {
    String rawMarc, tagsJson;

    String[] tags;

    if (data.input == null) {
      rawMarc = null;
      tags = new String[0];
    } else {
      rawMarc = ((String[]) data.input)[0];
      tagsJson = ((String[]) data.input)[1];

      List<String> list = new ArrayList<>();
      for (JsonNode n : MarcUtility.mapper.readTree(tagsJson)) {
        list.add(n.toString());
      }

      tags = MarcUtility.mapper.readValue(tagsJson, String[].class);
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

   static Stream<T<Object, String>> testGetFieldsFromMarcJsonStream() throws IOException {
    return Stream.of(
        new T<>(null, null, new NullPointerException()),
        new T<>(new String[] { "", "[]" }, null, new MarcException()),
        new T<>(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 050090245947980.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromMarcJsonStream")
  void testGetFieldsFromMarcJson(T<Object, String> data) throws JsonProcessingException {
    String marcJson, tagsJson;

    String[] tags;

    if (data.input == null) {
      marcJson = null;
      tags = new String[0];
    } else {
      marcJson = ((String[]) data.input)[0];
      tagsJson = ((String[]) data.input)[1];

      List<String> list = new ArrayList<>();
      for (JsonNode n : MarcUtility.mapper.readTree(tagsJson)) {
        list.add(n.toString());
      }

      tags = MarcUtility.mapper.readValue(tagsJson, String[].class);
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
