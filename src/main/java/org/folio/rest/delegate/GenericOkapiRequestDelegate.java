package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.model.FolioLogin;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.folio.rest.service.OkapiRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.camunda.spin.Spin.JSON;

@Service
public class GenericOkapiRequestDelegate extends AbstractRuntimeDelegate {

  private static final String REQUEST_URL = "requestUrl";
  private static final String REQUEST_METHOD = "requestMethod";
  private static final String REQUEST_PAYLOAD = "requestPayload";
  private static final String REQUEST_URI_VARIABLES = "requestUriVariables";

  private static final String REQUEST_CONTENT_TYPE = "requestContentType";

  private static final String RESPONSE_STATUS = "responseStatusName";
  private static final String RESPONSE_BODY = "responseBodyName";
  private static final String RESPONSE_HEADER = "responseHeaderName";

  @Value("${tenant.headerName:X-Okapi-Tenant}")
  private String tenantHeaderName;

  @Autowired
  private OkapiRequestService okapiRequestService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Generic Okapi Request Delegate");

    String tenant = execution.getTenantId();

    String okapiToken= "";
    if (execution.getVariable("folioLogin") != null) {
      FolioLogin folioLogin = (FolioLogin) execution.getVariable("folioLogin");
      okapiToken = folioLogin.getxOkapiToken();
    }

    SpinJsonNode jsonNode = JSON(execution.getVariable("okapiRequest"));

    // TODO: Refactor to map directly to OkapiRequest
    String requestUrl = jsonNode.prop(REQUEST_URL).stringValue();
    String requestMethod = jsonNode.prop(REQUEST_METHOD).stringValue();
    String requestContentType = jsonNode.prop(REQUEST_CONTENT_TYPE).stringValue();
    String responseStatusName = jsonNode.prop(RESPONSE_STATUS).stringValue();
    String responseHeaderName = jsonNode.prop(RESPONSE_HEADER).stringValue();
    String responseBodyName = jsonNode.prop(RESPONSE_BODY).stringValue();
    SpinJsonNode payload = jsonNode.prop(REQUEST_PAYLOAD);

    OkapiRequest okapiRequest = new OkapiRequest();
    okapiRequest.setTenant(tenant);
    okapiRequest.setRequestUrl(requestUrl);
    okapiRequest.setRequestMethod(requestMethod);
    okapiRequest.setRequestContentType(requestContentType);
    okapiRequest.setResponseStatusName(responseStatusName);
    okapiRequest.setResponseHeaderName(responseHeaderName);
    okapiRequest.setResponseBodyName(responseBodyName);
    okapiRequest.setRequestPayload(payload);
    okapiRequest.setOkapiToken(okapiToken);

    log.info("JSON: {}", jsonNode);
    log.info("payload: {}", payload);

    OkapiResponse okapiResponse = okapiRequestService.okapiRestCall(okapiRequest);
    log.info("OKAPI RESPONSE: {}", okapiResponse);
    okapiResponse.setBody(null);

    ObjectValue response = Variables.objectValue(okapiResponse)
      .serializationDataFormat("application/json")
      .create();

    execution.setVariable("okapiResponse", response);

  }

}
