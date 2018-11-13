package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
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
public class CrLostitemOkapiRequestDelegate extends AbstractRuntimeDelegate {

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
    // TODO: THIS REQUEST IS DOING A 'RENEW' IN FOLIO
    //       LOST ITEM IS NOT YET ACTIVE
    log.info("Executing Lost Item Okapi Request Delegate");

    String tenant = execution.getTenantId();
    String userId = execution.getVariable("userId").toString();
    String itemId = execution.getVariable("itemId").toString();

    String okapiToken = "";
    if (execution.getVariable("folioLogin") != null) {
      FolioLogin folioLogin = (FolioLogin) execution.getVariable("folioLogin");
      okapiToken = folioLogin.getxOkapiToken();
    }

    String requestUrl = "http://localhost:9130/circulation/renew-by-id";
    String requestMethod = "POST";
    String requestContentType = "application/json";
    String responseStatusName = "";
    String responseHeaderName = "";
    String responseBodyName = "";

    JSONObject json = new JSONObject();
    json.put("userId", userId);
    json.put("itemId", itemId);
    SpinJsonNode payload = JSON(json);

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

    log.info("payload: {}", payload);

    OkapiResponse okapiResponse = okapiRequestService.okapiRestCall(okapiRequest);
    log.info("OKAPI RESPONSE RENEW: {}", okapiResponse);

    ObjectValue response = Variables.objectValue(okapiResponse)
      .serializationDataFormat("application/json")
      .create();

    execution.setVariable("okapiResponseRenew", response);

  }

}
