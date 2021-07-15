package org.folio.rest.utility;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.folio.AlternativeTitleType;
import org.folio.Alternativetitletypes;
import org.folio.CallNumberType;
import org.folio.Callnumbertypes;
import org.folio.ClassificationType;
import org.folio.Classificationtypes;
import org.folio.ContributorNameType;
import org.folio.ContributorType;
import org.folio.Contributornametypes;
import org.folio.Contributortypes;
import org.folio.ElectronicAccessRelationship;
import org.folio.Electronicaccessrelationships;
import org.folio.HoldingsNoteType;
import org.folio.HoldingsType;
import org.folio.Holdingsnotetypes;
import org.folio.Holdingstypes;
import org.folio.IdentifierType;
import org.folio.Identifiertypes;
import org.folio.IllPolicy;
import org.folio.Illpolicies;
import org.folio.Instance;
import org.folio.InstanceFormat;
import org.folio.InstanceNoteType;
import org.folio.InstanceRelationshipType;
import org.folio.InstanceStatus;
import org.folio.InstanceType;
import org.folio.Instanceformats;
import org.folio.Instancenotetypes;
import org.folio.Instancerelationshiptypes;
import org.folio.Instancestatuses;
import org.folio.Instancetypes;
import org.folio.IssuanceMode;
import org.folio.Issuancemodes;
import org.folio.ItemDamageStatus;
import org.folio.ItemNoteType;
import org.folio.Itemdamagedstatuses;
import org.folio.Itemnotetypes;
import org.folio.Loantype;
import org.folio.Loantypes;
import org.folio.Location;
import org.folio.Locations;
import org.folio.MarcFieldProtectionSettingsCollection;
import org.folio.Materialtypes;
import org.folio.Mtype;
import org.folio.NatureOfContentTerm;
import org.folio.Natureofcontentterms;
import org.folio.StatisticalCode;
import org.folio.StatisticalCodeType;
import org.folio.Statisticalcodes;
import org.folio.Statisticalcodetypes;
import org.folio.processing.mapping.defaultmapper.MarcToInstanceMapper;
import org.folio.processing.mapping.defaultmapper.processor.parameters.MappingParameters;
import org.folio.rest.jaxrs.model.MarcFieldProtectionSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.vertx.core.json.JsonObject;

public class MappingUtility {

  private static final Logger log = LoggerFactory.getLogger(MappingUtility.class);

  private static final int SETTING_LIMIT = 1000;

  private static final String IDENTIFIER_TYPES_URL = "/identifier-types?limit=" + SETTING_LIMIT;
  private static final String CLASSIFICATION_TYPES_URL = "/classification-types?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_TYPES_URL = "/instance-types?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_FORMATS_URL = "/instance-formats?limit=" + SETTING_LIMIT;
  private static final String CONTRIBUTOR_TYPES_URL = "/contributor-types?limit=" + SETTING_LIMIT;
  private static final String CONTRIBUTOR_NAME_TYPES_URL = "/contributor-name-types?limit=" + SETTING_LIMIT;
  private static final String ELECTRONIC_ACCESS_URL = "/electronic-access-relationships?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_NOTE_TYPES_URL = "/instance-note-types?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_ALTERNATIVE_TITLE_TYPES_URL = "/alternative-title-types?limit=" + SETTING_LIMIT;
  private static final String ISSUANCE_MODES_URL = "/modes-of-issuance?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_STATUSES_URL = "/instance-statuses?limit=" + SETTING_LIMIT;
  private static final String NATURE_OF_CONTENT_TERMS_URL = "/nature-of-content-terms?limit=" + SETTING_LIMIT;
  private static final String INSTANCE_RELATIONSHIP_TYPES_URL = "/instance-relationship-types?limit=" + SETTING_LIMIT;
  private static final String HOLDINGS_TYPES_URL = "/holdings-types?limit=" + SETTING_LIMIT;
  private static final String HOLDINGS_NOTE_TYPES_URL = "/holdings-note-types?limit=" + SETTING_LIMIT;
  private static final String ILL_POLICIES_URL = "/ill-policies?limit=" + SETTING_LIMIT;
  private static final String CALL_NUMBER_TYPES_URL = "/call-number-types?limit=" + SETTING_LIMIT;
  private static final String STATISTICAL_CODES_URL = "/statistical-codes?limit=" + SETTING_LIMIT;
  private static final String STATISTICAL_CODE_TYPES_URL = "/statistical-code-types?limit=" + SETTING_LIMIT;
  private static final String LOCATIONS_URL = "/locations?limit=" + SETTING_LIMIT;
  private static final String MATERIAL_TYPES_URL = "/material-types?limit=" + SETTING_LIMIT;
  private static final String ITEM_DAMAGED_STATUSES_URL = "/item-damaged-statuses?limit=" + SETTING_LIMIT;
  private static final String LOAN_TYPES_URL = "/loan-types?limit=" + SETTING_LIMIT;
  private static final String ITEM_NOTE_TYPES_URL = "/item-note-types?limit=" + SETTING_LIMIT;
  private static final String FIELD_PROTECTION_SETTINGS_URL = "/field-protection-settings/marc?limit=" + SETTING_LIMIT;

  private static final RestTemplate restTemplate = new RestTemplate();

  private static final MarcToInstanceMapper marcToInstanceMapper = new MarcToInstanceMapper();

  private static final ObjectMapper objectMapper = new ObjectMapper();

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
    HttpHeaders headers = headers(tenant, token);
    return new MappingParameters()
      .withInitializedState(true)
      .withIdentifierTypes(getIdentifierTypes(okapiUrl, headers))
      .withClassificationTypes(getClassificationTypes(okapiUrl, headers))
      .withInstanceTypes(getInstanceTypes(okapiUrl, headers))
      .withElectronicAccessRelationships(getElectronicAccessRelationships(okapiUrl, headers))
      .withInstanceFormats(getInstanceFormats(okapiUrl, headers))
      .withContributorTypes(getContributorTypes(okapiUrl, headers))
      .withContributorNameTypes(getContributorNameTypes(okapiUrl, headers))
      .withInstanceNoteTypes(getInstanceNoteTypes(okapiUrl, headers))
      .withAlternativeTitleTypes(getAlternativeTitleTypes(okapiUrl, headers))
      .withIssuanceModes(getIssuanceModes(okapiUrl, headers))
      .withInstanceStatuses(getInstanceStatuses(okapiUrl, headers))
      .withNatureOfContentTerms(getNatureOfContentTerms(okapiUrl, headers))
      .withInstanceRelationshipTypes(getInstanceRelationshipTypes(okapiUrl, headers))
      .withInstanceRelationshipTypes(getInstanceRelationshipTypes(okapiUrl, headers))
      .withHoldingsTypes(getHoldingsTypes(okapiUrl, headers))
      .withHoldingsNoteTypes(getHoldingsNoteTypes(okapiUrl, headers))
      .withIllPolicies(getIllPolicies(okapiUrl, headers))
      .withCallNumberTypes(getCallNumberTypes(okapiUrl, headers))
      .withStatisticalCodes(getStatisticalCodes(okapiUrl, headers))
      .withStatisticalCodeTypes(getStatisticalCodeTypes(okapiUrl, headers))
      .withLocations(getLocations(okapiUrl, headers))
      .withMaterialTypes(getMaterialTypes(okapiUrl, headers))
      .withItemDamagedStatuses(getItemDamagedStatuses(okapiUrl, headers))
      .withLoanTypes(getLoanTypes(okapiUrl, headers))
      .withItemNoteTypes(getItemNoteTypes(okapiUrl, headers))
      .withMarcFieldProtectionSettings(getMarcFieldProtectionSettings(okapiUrl, headers));
  }
  
  private static List<IdentifierType> getIdentifierTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Identifiertypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + IDENTIFIER_TYPES_URL;
    ResponseEntity<Identifiertypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Identifiertypes.class);
    return response.getBody().getIdentifierTypes();
  }

  private static List<ClassificationType> getClassificationTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Classificationtypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + CLASSIFICATION_TYPES_URL;
    ResponseEntity<Classificationtypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Classificationtypes.class);
    return response.getBody().getClassificationTypes();
  }

  private static List<InstanceType> getInstanceTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Instancetypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + INSTANCE_TYPES_URL;
    ResponseEntity<Instancetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Instancetypes.class);
    return response.getBody().getInstanceTypes();
  }

  private static List<ElectronicAccessRelationship> getElectronicAccessRelationships(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Electronicaccessrelationships> entity = new HttpEntity<>(headers);
    String url = okapiUrl + ELECTRONIC_ACCESS_URL;
    ResponseEntity<Electronicaccessrelationships> response = restTemplate.exchange(url, HttpMethod.GET, entity, Electronicaccessrelationships.class);
    return response.getBody().getElectronicAccessRelationships();
  }

  private static List<InstanceFormat> getInstanceFormats(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Instanceformats> entity = new HttpEntity<>(headers);
    String url = okapiUrl + INSTANCE_FORMATS_URL;
    ResponseEntity<Instanceformats> response = restTemplate.exchange(url, HttpMethod.GET, entity, Instanceformats.class);
    return response.getBody().getInstanceFormats();
  }

  private static List<ContributorType> getContributorTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Contributortypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + CONTRIBUTOR_TYPES_URL;
    ResponseEntity<Contributortypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Contributortypes.class);
    return response.getBody().getContributorTypes();
  }

  private static List<ContributorNameType> getContributorNameTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Contributornametypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + CONTRIBUTOR_NAME_TYPES_URL;
    ResponseEntity<Contributornametypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Contributornametypes.class);
    return response.getBody().getContributorNameTypes();
  }

  private static List<InstanceNoteType> getInstanceNoteTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Instancenotetypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + INSTANCE_NOTE_TYPES_URL;
    ResponseEntity<Instancenotetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Instancenotetypes.class);
    return response.getBody().getInstanceNoteTypes();
  }

  private static List<AlternativeTitleType> getAlternativeTitleTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Alternativetitletypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + INSTANCE_ALTERNATIVE_TITLE_TYPES_URL;
    ResponseEntity<Alternativetitletypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Alternativetitletypes.class);
    return response.getBody().getAlternativeTitleTypes();
  }

  private static List<NatureOfContentTerm> getNatureOfContentTerms(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Natureofcontentterms> entity = new HttpEntity<>(headers);
    String url = okapiUrl + NATURE_OF_CONTENT_TERMS_URL;
    ResponseEntity<Natureofcontentterms> response = restTemplate.exchange(url, HttpMethod.GET, entity, Natureofcontentterms.class);
    return response.getBody().getNatureOfContentTerms();
  }

  private static List<InstanceStatus> getInstanceStatuses(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Instancestatuses> entity = new HttpEntity<>(headers);
    String url = okapiUrl + INSTANCE_STATUSES_URL;
    ResponseEntity<Instancestatuses> response = restTemplate.exchange(url, HttpMethod.GET, entity, Instancestatuses.class);
    return response.getBody().getInstanceStatuses();
  }

  private static List<InstanceRelationshipType> getInstanceRelationshipTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Instancerelationshiptypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + INSTANCE_RELATIONSHIP_TYPES_URL;
    ResponseEntity<Instancerelationshiptypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Instancerelationshiptypes.class);
    return response.getBody().getInstanceRelationshipTypes();
  }

  private static List<HoldingsType> getHoldingsTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Holdingstypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + HOLDINGS_TYPES_URL;
    ResponseEntity<Holdingstypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Holdingstypes.class);
    return response.getBody().getHoldingsTypes();
  }

  private static List<HoldingsNoteType> getHoldingsNoteTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Holdingsnotetypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + HOLDINGS_NOTE_TYPES_URL;
    ResponseEntity<Holdingsnotetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Holdingsnotetypes.class);
    return response.getBody().getHoldingsNoteTypes();
  }

  private static List<IllPolicy> getIllPolicies(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Illpolicies> entity = new HttpEntity<>(headers);
    String url = okapiUrl + ILL_POLICIES_URL;
    ResponseEntity<Illpolicies> response = restTemplate.exchange(url, HttpMethod.GET, entity, Illpolicies.class);
    return response.getBody().getIllPolicies();
  }

  private static List<CallNumberType> getCallNumberTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Callnumbertypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + CALL_NUMBER_TYPES_URL;
    ResponseEntity<Callnumbertypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Callnumbertypes.class);
    return response.getBody().getCallNumberTypes();
  }

  private static List<StatisticalCode> getStatisticalCodes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Statisticalcodes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + STATISTICAL_CODES_URL;
    ResponseEntity<Statisticalcodes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Statisticalcodes.class);
    return response.getBody().getStatisticalCodes();
  }

  private static List<StatisticalCodeType> getStatisticalCodeTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Statisticalcodetypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + STATISTICAL_CODE_TYPES_URL;
    ResponseEntity<Statisticalcodetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Statisticalcodetypes.class);
    return response.getBody().getStatisticalCodeTypes();
  }

  private static List<Location> getLocations(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Locations> entity = new HttpEntity<>(headers);
    String url = okapiUrl + LOCATIONS_URL;
    ResponseEntity<Locations> response = restTemplate.exchange(url, HttpMethod.GET, entity, Locations.class);
    return response.getBody().getLocations();
  }

  private static List<Mtype> getMaterialTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Materialtypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + MATERIAL_TYPES_URL;
    ResponseEntity<Materialtypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Materialtypes.class);
    return response.getBody().getMtypes();
  }

  private static List<ItemDamageStatus> getItemDamagedStatuses(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Itemdamagedstatuses> entity = new HttpEntity<>(headers);
    String url = okapiUrl + ITEM_DAMAGED_STATUSES_URL;
    ResponseEntity<Itemdamagedstatuses> response = restTemplate.exchange(url, HttpMethod.GET, entity, Itemdamagedstatuses.class);
    return response.getBody().getItemDamageStatuses();
  }

  private static List<Loantype> getLoanTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Loantypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + LOAN_TYPES_URL;
    ResponseEntity<Loantypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Loantypes.class);
    return response.getBody().getLoantypes();
  }

  private static List<ItemNoteType> getItemNoteTypes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Itemnotetypes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + ITEM_NOTE_TYPES_URL;
    ResponseEntity<Itemnotetypes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Itemnotetypes.class);
    return response.getBody().getItemNoteTypes();
  }

  private static List<MarcFieldProtectionSetting> getMarcFieldProtectionSettings(String okapiUrl, HttpHeaders headers) {
    HttpEntity<MarcFieldProtectionSettingsCollection> entity = new HttpEntity<>(headers);
    String url = okapiUrl + FIELD_PROTECTION_SETTINGS_URL;
    ResponseEntity<MarcFieldProtectionSettingsCollection> response = restTemplate.exchange(url, HttpMethod.GET, entity, MarcFieldProtectionSettingsCollection.class);
    return response.getBody().getMarcFieldProtectionSettings();
  }

  private static List<IssuanceMode> getIssuanceModes(String okapiUrl, HttpHeaders headers) {
    HttpEntity<Issuancemodes> entity = new HttpEntity<>(headers);
    String url = okapiUrl + ISSUANCE_MODES_URL;
    ResponseEntity<Issuancemodes> response = restTemplate.exchange(url, HttpMethod.GET, entity, Issuancemodes.class);
    return response.getBody().getIssuanceModes();
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

}
