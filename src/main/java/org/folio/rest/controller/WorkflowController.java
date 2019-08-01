package org.folio.rest.controller;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.spin.json.SpinJsonNode;
import org.folio.rest.annotation.TokenHeader;
import org.folio.rest.exception.UnableToActivateWorkflowException;
import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.model.OkapiRequest;
import org.folio.rest.model.OkapiResponse;
import org.folio.rest.model.Workflow;
import org.folio.rest.service.BpmnModelFactory;
import org.folio.rest.service.OkapiRequestService;
import org.folio.rest.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.camunda.spin.Spin.JSON;

@RestController
@RequestMapping("/workflows")
public class WorkflowController {

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private BpmnModelFactory bpmnModelFactory;

  @Autowired
  private OkapiRequestService okapiRequestService;

  @PostMapping("/{id}/activate")
  public Workflow activateWorkflow(
    @TenantHeader String tenant,
    @TokenHeader String token,
    @PathVariable String id,
    @RequestBody Workflow workflow
  ) throws WorkflowAlreadyActiveException, UnableToActivateWorkflowException {

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
    String requestMethod = "POST";
    String requestContentType = "application/json";
    String responseStatusName = "";
    String responseHeaderName = "";
    String responseBodyName = "";

    SpinJsonNode workflowNode = JSON(workflow);

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

    OkapiResponse response = okapiRequestService.okapiRestCall(request);

    if (response.getStatusCode() != 200) {
      throw new UnableToActivateWorkflowException(workflow.getId());
    }

    return workflow;
  }
}
