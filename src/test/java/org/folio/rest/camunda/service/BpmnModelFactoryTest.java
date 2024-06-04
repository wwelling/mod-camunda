package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.camunda.delegate.AbstractWorkflowDelegate;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class BpmnModelFactoryTest {

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private List<AbstractWorkflowDelegate> workflowDelegates;

  @InjectMocks
  private BpmnModelFactory bpmnModelFactory;

  private Workflow workflow;

  @BeforeEach
  void setUp() {
    workflow = mock(Workflow.class);
    when(workflow.getName()).thenReturn("Test Workflow");
    when(workflow.getHistoryTimeToLive()).thenReturn(10);
    when(workflow.getVersionTag()).thenReturn("1.0");
    when(workflow.getNodes()).thenReturn(Collections.emptyList());
  }

  @Test
  void testFromWorkflow() throws ScriptTaskDeserializeCodeFailure {
    BpmnModelInstance modelInstance = bpmnModelFactory.fromWorkflow(workflow);
    assertNotNull(modelInstance);
  }
}
