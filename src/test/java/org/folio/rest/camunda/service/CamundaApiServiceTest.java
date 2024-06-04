package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.camunda.exception.WorkflowAlreadyActiveException;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CamundaApiServiceTest {

  @Mock
  private BpmnModelFactory bpmnModelFactory;

  @InjectMocks
  private CamundaApiService camundaApiService;

  @Mock
  private ProcessEngine processEngine;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private DeploymentBuilder deploymentBuilder;

  @Mock
  private Deployment deployment;

  private Workflow workflow;

  @BeforeEach
  void setUp() throws ScriptTaskDeserializeCodeFailure {
    workflow = mock(Workflow.class);
    when(workflow.getName()).thenReturn("Test Workflow");
    when(workflow.isActive()).thenReturn(false);
    when(bpmnModelFactory.fromWorkflow(any(Workflow.class))).thenReturn(mock(BpmnModelInstance.class));
    when(processEngine.getRepositoryService()).thenReturn(repositoryService);
    when(repositoryService.createDeployment()).thenReturn(deploymentBuilder);
    when(deploymentBuilder.name(anyString())).thenReturn(deploymentBuilder);
    when(deploymentBuilder.addModelInstance(anyString(), any(BpmnModelInstance.class))).thenReturn(deploymentBuilder);
    when(deploymentBuilder.source(anyString())).thenReturn(deploymentBuilder);
    when(deploymentBuilder.tenantId(anyString())).thenReturn(deploymentBuilder);
    when(deploymentBuilder.deploy()).thenReturn(deployment);
    when(deployment.getId()).thenReturn("deploymentId");

    ReflectionTestUtils.setField(camundaApiService, "processEngine", processEngine);
  }

  @Test
  void testDeployWorkflow() throws ScriptTaskDeserializeCodeFailure, WorkflowAlreadyActiveException {
    String tenant = "testTenant";

    Workflow result = camundaApiService.deployWorkflow(workflow, tenant);

    assertNotNull(result);
    assertTrue(result.isActive());
    assertEquals("deploymentId", result.getDeploymentId());

    verify(bpmnModelFactory).fromWorkflow(workflow);
    verify(repositoryService).createDeployment();
  }

  @Test
  void testDeployWorkflowAlreadyActive() {
    when(workflow.isActive()).thenReturn(true);

    assertThrows(WorkflowAlreadyActiveException.class, () -> {
      camundaApiService.deployWorkflow(workflow, "testTenant");
    });
  }

  @Test
  void testUndeployWorkflow() {
    when(workflow.isActive()).thenReturn(true);
    when(workflow.getDeploymentId()).thenReturn("deploymentId");

    Workflow result = camundaApiService.undeployWorkflow(workflow);

    assertNotNull(result);
    assertFalse(result.isActive());
    assertNull(result.getDeploymentId());

    verify(repositoryService).deleteDeployment("deploymentId", true);
  }

  @Test
  void testUndeployWorkflowNotActive() {
    when(workflow.isActive()).thenReturn(false);

    Workflow result = camundaApiService.undeployWorkflow(workflow);

    assertNotNull(result);
    assertFalse(result.isActive());
    assertNull(result.getDeploymentId());

    verify(repositoryService, never()).deleteDeployment(anyString(), anyBoolean());
  }
}
