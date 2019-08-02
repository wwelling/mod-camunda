package org.folio.rest.service;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.exception.WorkflowAlreadyDeactivatedException;
import org.folio.rest.model.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CamundaApiService {

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Autowired
  private BpmnModelFactory bpmnModelFactory;

  public Workflow deployWorkflow(Workflow workflow)
    throws WorkflowAlreadyActiveException {

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

  public Workflow undeployWorkflow(Workflow workflow)
      throws WorkflowAlreadyDeactivatedException {

    if (!workflow.isActive()) {
      throw new WorkflowAlreadyDeactivatedException(workflow.getId());
    }

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    repositoryService.deleteDeployment(workflow.getDeploymentId());

    workflow.setActive(false);
    workflow.setDeploymentId(null);
    workflow.clearProcessDefinitionIds();

    return workflow;
  }
}