package org.folio.rest.controller;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.annotation.TokenHeader;
import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.model.Workflow;
import org.folio.rest.service.BpmnModelFactory;
import org.folio.rest.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflows")
public class WorkflowController {

  @Autowired
  private BpmnModelFactory bpmnModelFactory;

  @PostMapping("/{id}/activate")
  public Workflow activateWorkflow(
    @TenantHeader String tenant,
    @TokenHeader String token,
    @PathVariable String id,
    @RequestBody Workflow workflow
  ) throws WorkflowAlreadyActiveException {

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

    return workflow;
  }
}
