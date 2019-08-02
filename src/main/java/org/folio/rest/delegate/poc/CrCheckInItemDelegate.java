package org.folio.rest.delegate.poc;

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
public class CrCheckInItemDelegate extends AbstractRuntimeDelegate {

  @Value("${tenant.headerName:X-Okapi-Tenant}")
  private String tenantHeaderName;

  @Autowired
  private OkapiRequestService okapiRequestService;

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Check In Item Delegate");

    String tenant = execution.getTenantId();
    String loanId = execution.getProcessBusinessKey();
    String userId = execution.getVariable("userId").toString();
    String itemId = execution.getVariable("itemId").toString();
    SpinJsonNode checkOutJson = JSON(execution.getVariable("checkOutJson").toString());

    String okapiToken = "";
    if (execution.getVariable("folioLogin") != null) {
      FolioLogin folioLogin = (FolioLogin) execution.getVariable("folioLogin");
      okapiToken = folioLogin.getxOkapiToken();
    }

    String requestUrl = String.format("%s/circulation/loans/%s", OKAPI_LOCATION, loanId);
    log.info("requestUrl: {}", requestUrl);
    String requestMethod = "PUT";
    String requestContentType = "application/json";
    String responseStatusName = "";
    String responseHeaderName = "";
    String responseBodyName = "";

    JSONObject json = new JSONObject();
    json.put("id", loanId);
    json.put("userId", userId);
    json.put("itemId", itemId);

    // NOT NEEDED FOR CALL RIGHT NOW
    /*
    JSONObject item = new JSONObject();
    item.put("title", checkOutJson.prop("item").prop("title").stringValue());
    item.put("barcode", checkOutJson.prop("item").prop("barcode").stringValue());

    JSONObject itemStatus = new JSONObject();
    itemStatus.put("name", "Available");

    item.put("status", itemStatus);
    item.put("location", checkOutJson.prop("item").prop("location").stringValue());
    json.put("item", item);
    */

    json.put("loanDate", checkOutJson.prop("loanDate").stringValue());
    //json.put("returnDate", "2018-11-14T09:15:23Z");

    JSONObject status = new JSONObject();
    status.put("name", "Closed");

    json.put("status", status);
    json.put("action", "checkedin");

    SpinJsonNode payload = JSON(json.toString());

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
    log.info("OKAPI RESPONSE CHECK IN: {}", okapiResponse);

    ObjectValue response = Variables.objectValue(okapiResponse)
      .serializationDataFormat("application/json")
      .create();

    execution.setVariable("okapiResponseCheckIn", response);

  }

}
