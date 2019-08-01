package org.folio.rest.service;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.exception.UnableToActivateWorkflowException;
import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.folio.rest.model.Workflow;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.camunda.spin.Spin.JSON;

@Service
public class CamundaApiService {

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private BpmnModelFactory bpmnModelFactory;

  @Autowired
  private OkapiRequestService okapiRequestService;

  public Workflow deployWorkflow(Workflow workflow, String tenant, String token, String id)
      throws WorkflowAlreadyActiveException, UnableToActivateWorkflowException {

  if (workflow.isActive()) {
      throw new WorkflowAlreadyActiveException(workflow.getId());
    }
    
    BpmnModelInstance modelInstance = bpmnModelFactory.makeBPMNFromWorkflow(workflow);

    Bpmn.validateModel(modelInstance);

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance(workflow.getName().replace(" ", "") + ".bpmn", modelInstance)
      .deploy();

    workflow.setActive(true);
    String deploymentId = deployment.getId();
    workflow.setDeploymentId(deploymentId);
    List<ProcessDefinition> deployedProcessDefinitions = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).list();
    for (ProcessDefinition processDefinition : deployedProcessDefinitions) {
      workflow.addProcessDefinitionId(processDefinition.getId());
    }

    String requestUrl = String.format("%s/workflow/workflow/%s/activate", OKAPI_LOCATION, id);

    SpinJsonNode workflowNode = JSON(workflow);

    OkapiResponse response = sendDeploymentRequest(workflowNode, tenant, requestUrl, token);

    if (response.getStatusCode() != 200) {
      throw new UnableToActivateWorkflowException(workflow.getId());
    }

    return workflow;
  }

  private OkapiResponse sendDeploymentRequest(SpinJsonNode workflowNode, String tenant, String requestUrl, String token) {

    String requestMethod = "POST";
    String requestContentType = "application/json";
    String responseStatusName = "";
    String responseHeaderName = "";
    String responseBodyName = "";

    OkapiRequest request = new OkapiRequest();
    request.setTenant(tenant);
    request.setRequestUrl(requestUrl);
    request.setRequestMethod(requestMethod);
    request.setRequestContentType(requestContentType);
    request.setResponseStatusName(responseStatusName);
    request.setResponseHeaderName(responseHeaderName);
    request.setResponseBodyName(responseBodyName);
    request.setRequestPayload(workflowNode);
    request.setOkapiToken(token);

    return okapiRequestService.okapiRestCall(request);
  }
}