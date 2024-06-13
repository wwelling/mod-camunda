package org.folio.rest.camunda.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.json.DecodeException;
import org.apache.commons.io.IOUtils;
import org.folio.Alternativetitletypes;
import org.folio.Callnumbertypes;
import org.folio.Classificationtypes;
import org.folio.Contributornametypes;
import org.folio.Contributortypes;
import org.folio.Electronicaccessrelationships;
import org.folio.Holdingsnotetypes;
import org.folio.Holdingstypes;
import org.folio.Identifiertypes;
import org.folio.Illpolicies;
import org.folio.Instanceformats;
import org.folio.Instancenotetypes;
import org.folio.Instancerelationshiptypes;
import org.folio.Instancestatuses;
import org.folio.Instancetypes;
import org.folio.Issuancemodes;
import org.folio.Itemdamagedstatuses;
import org.folio.Itemnotetypes;
import org.folio.Loantypes;
import org.folio.Locations;
import org.folio.MarcFieldProtectionSettingsCollection;
import org.folio.Materialtypes;
import org.folio.Natureofcontentterms;
import org.folio.Statisticalcodes;
import org.folio.Statisticalcodetypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class MappingUtilityTest {

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

  @Spy
  private RestTemplate mockRestTemplate;

  private static ObjectMapper objectMapper = new ObjectMapper();;

  private final static String OKAPI_URL = "http://localhost:9130";

  /** input */
  @SuppressWarnings("hiding")
  static <T> ResponseEntity<T> i(String path, Class<T> valueType) throws IOException {
    return ResponseEntity.ofNullable(objectMapper.readValue(new File("src/test/resources/" + path), valueType));
  }

  /** input */
  static String i(String path) throws IOException {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
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

  @BeforeEach
  void mockExchangeForRulesAndMappingParameters() throws RestClientException, IOException {
    lenient().doReturn(i("/folio/settings/rules.json", String.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.MAPPING_RULES_URL), eq(HttpMethod.GET), any(), eq(String.class));
    lenient().doReturn(i("/folio/settings/Identifiertypes.json", Identifiertypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.IDENTIFIER_TYPES_URL), eq(HttpMethod.GET), any(), eq(Identifiertypes.class));
    lenient().doReturn(i("/folio/settings/Classificationtypes.json", Classificationtypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.CLASSIFICATION_TYPES_URL), eq(HttpMethod.GET), any(), eq(Classificationtypes.class));
    lenient().doReturn(i("/folio/settings/Instancetypes.json", Instancetypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.INSTANCE_TYPES_URL), eq(HttpMethod.GET), any(), eq(Instancetypes.class));
    lenient().doReturn(i("/folio/settings/Electronicaccessrelationships.json", Electronicaccessrelationships.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.ELECTRONIC_ACCESS_URL), eq(HttpMethod.GET), any(), eq(Electronicaccessrelationships.class));
    lenient().doReturn(i("/folio/settings/Instanceformats.json", Instanceformats.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.INSTANCE_FORMATS_URL), eq(HttpMethod.GET), any(), eq(Instanceformats.class));
    lenient().doReturn(i("/folio/settings/Contributortypes.json", Contributortypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.CONTRIBUTOR_TYPES_URL), eq(HttpMethod.GET), any(), eq(Contributortypes.class));
    lenient().doReturn(i("/folio/settings/Contributornametypes.json", Contributornametypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.CONTRIBUTOR_NAME_TYPES_URL), eq(HttpMethod.GET), any(), eq(Contributornametypes.class));
    lenient().doReturn(i("/folio/settings/Instancenotetypes.json", Instancenotetypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.INSTANCE_NOTE_TYPES_URL), eq(HttpMethod.GET), any(), eq(Instancenotetypes.class));
    lenient().doReturn(i("/folio/settings/Alternativetitletypes.json", Alternativetitletypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.INSTANCE_ALTERNATIVE_TITLE_TYPES_URL), eq(HttpMethod.GET), any(), eq(Alternativetitletypes.class));
    lenient().doReturn(i("/folio/settings/Natureofcontentterms.json", Natureofcontentterms.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.NATURE_OF_CONTENT_TERMS_URL), eq(HttpMethod.GET), any(), eq(Natureofcontentterms.class));
    lenient().doReturn(i("/folio/settings/Instancestatuses.json", Instancestatuses.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.INSTANCE_STATUSES_URL), eq(HttpMethod.GET), any(), eq(Instancestatuses.class));
    lenient().doReturn(i("/folio/settings/Instancerelationshiptypes.json", Instancerelationshiptypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.INSTANCE_RELATIONSHIP_TYPES_URL), eq(HttpMethod.GET), any(), eq(Instancerelationshiptypes.class));
    lenient().doReturn(i("/folio/settings/Holdingstypes.json", Holdingstypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.HOLDINGS_TYPES_URL), eq(HttpMethod.GET), any(), eq(Holdingstypes.class));
    lenient().doReturn(i("/folio/settings/Holdingsnotetypes.json", Holdingsnotetypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.HOLDINGS_NOTE_TYPES_URL), eq(HttpMethod.GET), any(), eq(Holdingsnotetypes.class));
    lenient().doReturn(i("/folio/settings/Illpolicies.json", Illpolicies.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.ILL_POLICIES_URL), eq(HttpMethod.GET), any(), eq(Illpolicies.class));
    lenient().doReturn(i("/folio/settings/Callnumbertypes.json", Callnumbertypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.CALL_NUMBER_TYPES_URL), eq(HttpMethod.GET), any(), eq(Callnumbertypes.class));
    lenient().doReturn(i("/folio/settings/Statisticalcodes.json", Statisticalcodes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.STATISTICAL_CODES_URL), eq(HttpMethod.GET), any(), eq(Statisticalcodes.class));
    lenient().doReturn(i("/folio/settings/Statisticalcodetypes.json", Statisticalcodetypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.STATISTICAL_CODE_TYPES_URL), eq(HttpMethod.GET), any(), eq(Statisticalcodetypes.class));
    lenient().doReturn(i("/folio/settings/Locations.json", Locations.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.LOCATIONS_URL), eq(HttpMethod.GET), any(), eq(Locations.class));
    lenient().doReturn(i("/folio/settings/Materialtypes.json", Materialtypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.MATERIAL_TYPES_URL), eq(HttpMethod.GET), any(), eq(Materialtypes.class));
    lenient().doReturn(i("/folio/settings/Itemdamagedstatuses.json", Itemdamagedstatuses.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.ITEM_DAMAGED_STATUSES_URL), eq(HttpMethod.GET), any(), eq(Itemdamagedstatuses.class));
    lenient().doReturn(i("/folio/settings/Loantypes.json", Loantypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.LOAN_TYPES_URL), eq(HttpMethod.GET), any(), eq(Loantypes.class));
    lenient().doReturn(i("/folio/settings/Itemnotetypes.json", Itemnotetypes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.ITEM_NOTE_TYPES_URL), eq(HttpMethod.GET), any(), eq(Itemnotetypes.class));
    lenient().doReturn(i("/folio/settings/MarcFieldProtectionSettingsCollection.json", MarcFieldProtectionSettingsCollection.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.FIELD_PROTECTION_SETTINGS_URL), eq(HttpMethod.GET), any(), eq(MarcFieldProtectionSettingsCollection.class));
    lenient().doReturn(i("/folio/settings/Issuancemodes.json", Issuancemodes.class)).when(mockRestTemplate).exchange(eq(OKAPI_URL + MappingUtility.ISSUANCE_MODES_URL), eq(HttpMethod.GET), any(), eq(Issuancemodes.class));
    MappingUtility.restTemplate = mockRestTemplate;
  }

  /**************************************************************************************
   * mapRecordToInsance                                                                 *
   *************************************************************************************/

  @ParameterizedTest
  @MethodSource("testMapRecordToInsanceStream")
  void testMapRecordToInsance(T<String, String> data) throws JsonProcessingException {

    String marcJson = data.input;
    String okapiUrl = OKAPI_URL;
    String tenant = "diku";
    String token = "";

    if (Objects.nonNull(data.exception)) {
      assertThrows(data.exception.getClass(), () -> MappingUtility.mapRecordToInsance(marcJson, okapiUrl, tenant, token));
    } else {
      ObjectNode expected = (ObjectNode) om(data.expected);
      expected.remove("id");
      ObjectNode actual = (ObjectNode) om(MappingUtility.mapRecordToInsance(marcJson, okapiUrl, tenant, token));
      actual.remove("id");

      assertEquals(expected, actual);
    }
  }

  private static Stream<T<String, String>> testMapRecordToInsanceStream() throws IOException {
    return Stream.of(
      new T<String, String>(null, null, new NullPointerException()),
      new T<String, String>("", null, new DecodeException()),
      new T<String, String>(i("/marc4j/54-56-008008027-0.mrc.json"), i("/folio/instances/54-008008027.json")),
      new T<String, String>(i("/marc4j/54-56-008008027-1.mrc.json"), i("/folio/instances/55-008008027.json")),
      new T<String, String>(i("/marc4j/54-56-008008027-2.mrc.json"), i("/folio/instances/56-008008027.json")),
      new T<String, String>(i("/marc4j/54-56-008008027-3.mrc.json"), i("/folio/instances/57-008008027.json")),
      new T<String, String>(i("/marc4j/54-56-008008027-4.mrc.json"), i("/folio/instances/58-008008027.json"))
    );
  }

}
