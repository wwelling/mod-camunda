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
import org.folio.rest.workflow.components.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CamundaApiService {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${okapi.location}")
  private String OKAPI_LOCATION;

  @Value("${tenant.default-tenant}")
  private String TENANT_NAME;

  @Autowired
  private BpmnModelFactory bpmnModelFactory;

  public Workflow deployWorkflow(Workflow workflow) throws WorkflowAlreadyActiveException {

    if (workflow.isActive()) {
      throw new WorkflowAlreadyActiveException(workflow.getId());
    }

    BpmnModelInstance modelInstance = bpmnModelFactory.fromWorkflow(workflow);

    Bpmn.validateModel(modelInstance);

    log.info("BPMN: {}", Bpmn.convertToString(modelInstance));

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();

    Deployment deployment = repositoryService.createDeployment()
        .addModelInstance(workflow.getName().replace(" ", "") + ".bpmn", modelInstance)
        .tenantId(TENANT_NAME)
        .deploy();

    workflow.setActive(true);
    String deploymentId = deployment.getId();
    workflow.setDeploymentId(deploymentId);
    List<ProcessDefinition> deployedProcessDefinitions = repositoryService.createProcessDefinitionQuery()
        .deploymentId(deploymentId)
        .list();

    for (ProcessDefinition processDefinition : deployedProcessDefinitions) {
      workflow.getProcessDefinitionIds().add(processDefinition.getId());
    }

    return workflow;
  }

  public Workflow undeployWorkflow(Workflow workflow) throws WorkflowAlreadyDeactivatedException {

    if (!workflow.isActive()) {
      throw new WorkflowAlreadyDeactivatedException(workflow.getId());
    }

    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    repositoryService.deleteDeployment(workflow.getDeploymentId());

    workflow.setActive(false);
    workflow.setDeploymentId(null);
    workflow.getProcessDefinitionIds().clear();

    return workflow;
  }

}