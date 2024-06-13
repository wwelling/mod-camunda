package org.folio.rest.camunda.utility;

import static org.folio.rest.camunda.utility.TestUtility.i;
import static org.folio.rest.camunda.utility.TestUtility.il;
import static org.folio.rest.camunda.utility.TestUtility.om;
import static org.folio.rest.camunda.utility.TestUtility.oml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

  /**************************************************************************************
   * splitRawMarcToMarcJsonRecords                                                      *
   *************************************************************************************/

  static Stream<Parameters<String, List<String>>> testSplitRawMarcToMarcJsonRecordsStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of("", List.of()),
        Parameters.of(i("/marc4j/54-56-008008027.mrc"), il("/marc4j/54-56-008008027.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testSplitRawMarcToMarcJsonRecordsStream")
  void testSplitRawMarcToMarcJsonRecords(Parameters<String, List<String>> data) {
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

  static Stream<Parameters<Object, String>> testAddFieldToMarcJsonStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new IllegalArgumentException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/field/999.mrc.json"), i("/marc4j/withfields/54-56-008008027+999.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testAddFieldToMarcJsonStream")
  void testAddFieldToMarcJson(Parameters<Object, String> data) {
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

  static Stream<Parameters<String, String>> testUpdateControlNumberFieldStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json")),
        Parameters.of(i("/marc4j/54-56-008008027-0-001.mrc.json"), i("/marc4j/withcontrolnumber/54-56-008008027+001.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testUpdateControlNumberFieldStream")
  void testUpdateControlNumberField(Parameters<String, String> data) {
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

  static Stream<Parameters<String, String>> testMarcJsonToRawMarcStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of("", null, new MarcException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json"), i("/marc4j/54-56-008008027-0.mrc"))
      );
  }

  @ParameterizedTest
  @MethodSource("testMarcJsonToRawMarcStream")
  void testMarcJsonToRawMarc(Parameters<String, String> data) {
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

  static Stream<Parameters<String, String>> testRawMarcToMarcJsonStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of("", null, new MarcException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc"), i("/marc4j/54-56-008008027-0.mrc.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testRawMarcToMarcJsonStream")
  void testRawMarcToMarcJson(Parameters<String, String> data) {
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

  static Stream<Parameters<Object, String>> testGetFieldsFromRawMarcStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of(new String[] { "", "[]" }, null, new MarcException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 050090245947980.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromRawMarcStream")
  void testGetFieldsFromRawMarc(Parameters<Object, String> data) throws JsonProcessingException {
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

  static Stream<Parameters<Object, String>> testGetFieldsFromMarcJsonStream() throws IOException {
    return Stream.of(
        Parameters.of(null, null, new NullPointerException()),
        Parameters.of(new String[] { "", "[]" }, null, new MarcException()),
        Parameters.of(i("/marc4j/54-56-008008027-0.mrc.json", "/marc4j/tags/050090245947980.json"), i("/marc4j/fields/54-56-008008027 050090245947980.json"))
      );
  }

  @ParameterizedTest
  @MethodSource("testGetFieldsFromMarcJsonStream")
  void testGetFieldsFromMarcJson(Parameters<Object, String> data) throws JsonProcessingException {
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
