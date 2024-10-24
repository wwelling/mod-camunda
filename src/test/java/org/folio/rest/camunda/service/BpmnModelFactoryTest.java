package org.folio.rest.camunda.service;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.ServiceTaskBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.camunda.delegate.AbstractWorkflowDelegate;
import org.folio.rest.camunda.delegate.InputDelegate;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.workflow.model.EndEvent;
import org.folio.rest.workflow.model.InputTask;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.Setup;
import org.folio.rest.workflow.model.StartEvent;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BpmnModelFactoryTest {

  @Mock
  private BpmnModelInstance bpmnModelInstance;

  @Mock
  private CamundaField camundaField;

  @Mock
  private ExtensionElements extensionElements;

  @Mock
  private ModelElementInstance modelElementInstance;

  @Mock
  private Process process;

  @Mock
  private ProcessBuilder processBuilderMocked;

  @Mock
  private StartEventBuilder startEventBuilder;

  @Mock
  private ServiceTaskBuilder serviceTaskBuilder;

  @Mock
  private UserTaskBuilder userTaskBuilder;

  @Mock
  private EndEventBuilder endEventBuilder;

  @Spy
  private ObjectMapper objectMapper;

  @Spy
  private List<AbstractWorkflowDelegate> workflowDelegates;

  @InjectMocks
  private BpmnModelFactory bpmnModelFactory;

  private ProcessBuilder processBuilder;

  private Setup setup;

  private Workflow workflow;

  @BeforeEach
  void beforeEach() {
    setup = new Setup();

    workflow = new Workflow();
    workflow.setId(UUID);
    workflow.setSetup(setup);

    processBuilder = new ProcessBuilder(bpmnModelInstance, process);
  }

  @Test
  void testFromWorkflowException() throws ScriptTaskDeserializeCodeFailure {
    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      commonUnmockedProcessBuilder(utility);
      commonMockingsBasic();

      assertNotNull(bpmnModelFactory.fromWorkflow(workflow));
    }
  }

  @Test
  void testFromWorkflowNoNodesWorks() throws ScriptTaskDeserializeCodeFailure {
    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      commonUnmockedProcessBuilder(utility);
      commonMockingsBasic();

      assertNotNull(bpmnModelFactory.fromWorkflow(workflow));
    }
  }

  @Test
  void testFromWorkflowMustStartWithStartEvent() {

    Node node = new InputTask();
    node.setId("3b7060b9-523f-4279-86bd-e8ef1e4eff21");
    node.setName("input");
    List<Node> nodes = new ArrayList<>();
    nodes.add(node);

    workflow.setNodes(nodes);

    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      commonMockedProcessBuilder(utility);

      when(processBuilderMocked.startEvent()).thenReturn(startEventBuilder);

      RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
        bpmnModelFactory.fromWorkflow(workflow);
      });

      assertNotNull(exception);
      assertTrue(exception.getMessage().contains("Workflow must start with a start event"));
    }
  }

  @Test
  void testFromWorkflow() throws ScriptTaskDeserializeCodeFailure {
    Node start = new StartEvent();
    start.setId("8aeab6f7-5b51-42f2-bf79-819dc50253db");
    start.setName("start");

    Node input = new InputTask();
    input.setId("789ee60d-e24f-4c40-adcb-984d68fab24d");
    input.setName("input");

    Node end = new EndEvent();
    end.setId("ea99f40c-832d-4f2c-b81b-cf4c1638daa5");
    end.setName("end");

    List<Node> nodes = new ArrayList<>();
    nodes.add(start);
    nodes.add(input);
    nodes.add(end);

    workflow.setNodes(nodes);

    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      commonMockedProcessBuilder(utility);
      commonMockingsBasic();

      when(processBuilderMocked.startEvent()).thenReturn(startEventBuilder);

      when(workflowDelegates.stream()).thenAnswer(invocation -> {
        List<AbstractWorkflowDelegate> mockDelegates = new ArrayList<>();
        mockDelegates.add(new InputDelegate());
        return mockDelegates.stream();
    });

      assertNotNull(bpmnModelFactory.fromWorkflow(workflow));
    }
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   *
   * This function uses the instantiated and unmocked ProcessBuilder.
   *
   * @param utility The mocked static bmp class utility instance.
   */
  private void commonUnmockedProcessBuilder(MockedStatic<Bpmn> utility) {
    utility.when(() -> Bpmn.createExecutableProcess()).thenReturn(processBuilder);
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   *
   * This function uses the mocked ProcessBuilder.
   *
   * @param utility The mocked static bmp class utility instance.
   */
  private void commonMockedProcessBuilder(MockedStatic<Bpmn> utility) {
    utility.when(() -> Bpmn.createExecutableProcess()).thenReturn(processBuilderMocked);

    lenient().when(startEventBuilder.id(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.name(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.message(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.timerWithCycle(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.signal(anyString())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.interrupting(anyBoolean())).thenReturn(startEventBuilder);
    lenient().when(startEventBuilder.serviceTask(anyString())).thenReturn(serviceTaskBuilder);
    lenient().when(startEventBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(serviceTaskBuilder.name(anyString())).thenReturn(serviceTaskBuilder);
    lenient().when(serviceTaskBuilder.camundaDelegateExpression(anyString())).thenReturn(serviceTaskBuilder);
    lenient().when(serviceTaskBuilder.userTask(anyString())).thenReturn(userTaskBuilder);
    lenient().when(serviceTaskBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(userTaskBuilder.endEvent(anyString())).thenReturn(endEventBuilder);
    lenient().when(userTaskBuilder.name(anyString())).thenReturn(userTaskBuilder);
    lenient().when(userTaskBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(endEventBuilder.name(anyString())).thenReturn(endEventBuilder);
    lenient().when(endEventBuilder.done()).thenReturn(bpmnModelInstance);

    lenient().when(processBuilderMocked.startEvent()).thenReturn(startEventBuilder);

    lenient().when(processBuilderMocked.getElement()).thenReturn(process);
    lenient().when(processBuilderMocked.name(any())).thenReturn(processBuilderMocked);
    lenient().when(processBuilderMocked.camundaHistoryTimeToLive(anyInt())).thenReturn(processBuilderMocked);
    lenient().when(processBuilderMocked.camundaVersionTag(any())).thenReturn(processBuilderMocked);
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   */
  private void commonMockingsBasic() {
    when(bpmnModelInstance.newInstance(ExtensionElements.class))
        .thenReturn(extensionElements);
    when(bpmnModelInstance.newInstance(CamundaField.class))
        .thenReturn(camundaField);
    doNothing().when(extensionElements).addChildElement(any());
    when(bpmnModelInstance.getModelElementById(anyString())).thenReturn(modelElementInstance);
  }

}
