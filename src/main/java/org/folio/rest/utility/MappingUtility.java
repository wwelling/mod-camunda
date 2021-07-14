package org.folio.rest.utility;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections4.list.UnmodifiableList;
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
import org.folio.Instance;
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
import org.folio.Materialtypes;
import org.folio.Natureofcontentterms;
import org.folio.Statisticalcodes;
import org.folio.Statisticalcodetypes;
import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.vertx.core.json.JsonObject;

public class MappingUtility {

  private static final Logger log = LoggerFactory.getLogger(MappingUtility.class);

  private static final RestTemplate restTemplate = new RestTemplate();

  private static final MarcToInstanceMapper marcToInstanceMapper = new MarcToInstanceMapper();

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final int SETTING_LIMIT = 1000;

  private MappingUtility() {

  }

  public static String mapRecordToInsance(String marcJson, String okapiUrl, String tenant, String token) throws JsonProcessingException {
    JsonObject parsedRecord = new JsonObject(marcJson);
    JsonObject mappingRules = fetchRules(okapiUrl, tenant, token);
    MappingParameters mappingParameters = getMappingParamaters(okapiUrl, tenant, token);
    Instance instance = marcToInstanceMapper.mapRecord(parsedRecord, mappingParameters, mappingRules);
    return objectMapper.writeValueAsString(instance);
  }

  private static JsonObject fetchRules(String okapiUrl, String tenant, String token) {
    HttpEntity<?> entity = new HttpEntity<>(headers(tenant, token));
    String url = okapiUrl + "/mapping-rules";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    return new JsonObject(response.getBody());
  }

  private static MappingParameters getMappingParamaters(String okapiUrl, String tenant, String token) {
    final MappingParameters mappingParameters = new MappingParameters();
    // @formatter:off
    Arrays.asList(new ReferenceFetcher[] {
      new ReferenceFetcher("/identifier-types?limit=" + SETTING_LIMIT, Identifiertypes.class, "identifierTypes"),
      new ReferenceFetcher("/classification-types?limit=" + SETTING_LIMIT, Classificationtypes.class, "classificationTypes"),
      new ReferenceFetcher("/instance-types?limit=" + SETTING_LIMIT, Instancetypes.class, "instanceTypes"),
      new ReferenceFetcher("/electronic-access-relationships?limit=" + SETTING_LIMIT, Electronicaccessrelationships.class, "electronicAccessRelationships"),
      new ReferenceFetcher("/instance-formats?limit=" + SETTING_LIMIT, Instanceformats.class, "instanceFormats"),
      new ReferenceFetcher("/contributor-types?limit=" + SETTING_LIMIT, Contributortypes.class, "contributorTypes"),
      new ReferenceFetcher("/contributor-name-types?limit=" + SETTING_LIMIT, Contributornametypes.class, "contributorNameTypes"),
      new ReferenceFetcher("/instance-note-types?limit=" + SETTING_LIMIT, Instancenotetypes.class, "instanceNoteTypes"),
      new ReferenceFetcher("/alternative-title-types?limit=" + SETTING_LIMIT, Alternativetitletypes.class, "alternativeTitleTypes"),
      new ReferenceFetcher("/modes-of-issuance?limit=" + SETTING_LIMIT, Issuancemodes.class, "issuanceModes"),
      new ReferenceFetcher("/instance-statuses?limit=" + SETTING_LIMIT, Instancestatuses.class, "instanceStatuses"),
      new ReferenceFetcher("/nature-of-content-terms?limit=" + SETTING_LIMIT, Natureofcontentterms.class, "natureOfContentTerms"),
      new ReferenceFetcher("/instance-relationship-types?limit=" + SETTING_LIMIT, Instancerelationshiptypes.class, "instanceRelationshipTypes"),
      new ReferenceFetcher("/holdings-types?limit=" + SETTING_LIMIT, Holdingstypes.class, "holdingsTypes"),
      new ReferenceFetcher("/holdings-note-types?limit=" + SETTING_LIMIT, Holdingsnotetypes.class, "holdingsNoteTypes"),
      new ReferenceFetcher("/ill-policies?limit=" + SETTING_LIMIT, Illpolicies.class, "illPolicies"),
      new ReferenceFetcher("/call-number-types?limit=" + SETTING_LIMIT, Callnumbertypes.class, "callNumberTypes"),
      new ReferenceFetcher("/statistical-codes?limit=" + SETTING_LIMIT, Statisticalcodes.class, "statisticalCodes"),
      new ReferenceFetcher("/statistical-code-types?limit=" + SETTING_LIMIT, Statisticalcodetypes.class, "statisticalCodeTypes"),
      new ReferenceFetcher("/locations?limit=" + SETTING_LIMIT, Locations.class, "locations"),
      new ReferenceFetcher("/material-types?limit=" + SETTING_LIMIT, Materialtypes.class, "mtypes"),
      new ReferenceFetcher("/item-damaged-statuses?limit=" + SETTING_LIMIT, Itemdamagedstatuses.class, "itemDamageStatuses"),
      new ReferenceFetcher("/loan-types?limit=" + SETTING_LIMIT, Loantypes.class, "loanTypes"),
      new ReferenceFetcher("/item-note-types?limit=" + SETTING_LIMIT, Itemnotetypes.class, "itemNoteTypes")
    }).forEach(fetcher -> {
      HttpEntity<JsonNode> entity = new HttpEntity<JsonNode>(headers(tenant, token));
      String url = okapiUrl + fetcher.getUrl();
      Class<?> collectionType = fetcher.getCollectionType();
      ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.GET, entity, collectionType);
      try {
        Field source = collectionType.getDeclaredField(fetcher.getProperty());
        source.setAccessible(true);
        Field target = mappingParameters.getClass().getDeclaredField(fetcher.getProperty());
        target.setAccessible(true);
        target.set(mappingParameters, new UnmodifiableList<>((List<?>) source.get(response.getBody())));
      } catch (RestClientException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    });
    mappingParameters.setInitialized(true);
    // @formatter:on
    return mappingParameters;
  }

  private static HttpHeaders headers(String tenant, String token) {
    HttpHeaders headers = headers(tenant);
    headers.set("X-Okapi-Token", token);
    return headers;
  }

  // NOTE: assuming all accept and content type will be application/json
  private static HttpHeaders headers(String tenant) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-Okapi-Tenant", tenant);
    return headers;
  }

  private static class ReferenceFetcher {

    private final String url;

    private final Class<?> collectionType;

    private final String property;

    public ReferenceFetcher(String url, Class<?> collectionType, String property) {
      this.url = url;
      this.collectionType = collectionType;
      this.property = property;
    }

    public String getUrl() {
      return url;
    }

    public Class<?> getCollectionType() {
      return collectionType;
    }

    public String getProperty() {
      return property;
    }

  }

}
