package org.folio.rest.camunda.service;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.workflow.model.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CamundaApiService {

  @Autowired
  private BpmnModelFactory bpmnModelFactory;

  public Workflow deployWorkflow(Workflow workflow, String tenant) throws WorkflowAlreadyActiveException, ScriptTaskDeserializeCodeFailure {
    if (Boolean.TRUE.equals(workflow.getActive())) {
      throw new WorkflowAlreadyActiveException(workflow.getId());
    }

    BpmnModelInstance modelInstance = bpmnModelFactory.fromWorkflow(workflow);

    Bpmn.validateModel(modelInstance);

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment().name(workflow.getName())
      .addModelInstance(workflow.getName().replace(" ", "") + ".bpmn", modelInstance)
      .source("mod-workflow")
      .tenantId(tenant)
      .deploy();

    String deploymentId = deployment.getId();

    workflow.setActive(true);
    workflow.setDeploymentId(deploymentId);

    return workflow;
  }

  public Workflow undeployWorkflow(Workflow workflow) {
    if (Boolean.FALSE.equals(workflow.getActive())) {
      return workflow;
    }

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    repositoryService.deleteDeployment(workflow.getDeploymentId(), true);

    workflow.setActive(false);
    workflow.setDeploymentId(null);

    return workflow;
  }

}
