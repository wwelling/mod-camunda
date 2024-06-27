package org.folio.rest.camunda.service;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.workflow.model.Node;
import org.folio.rest.workflow.model.Setup;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BpmnModelFactoryTest {

  @Mock
  private AbstractFlowNodeBuilder<?, ?> abstractFlowNodeBuilder;

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

  @Spy
  private ObjectMapper objectMapper;

  @InjectMocks
  private BpmnModelFactory bpmnModelFactory;

  private ProcessBuilder processBuilder;

  private Node node;

  private List<Node> nodes;

  private Setup setup;

  private Workflow workflow;

  @BeforeEach
  void beforeEach() {
    node = new MyNode();
    nodes = new ArrayList<>();
    nodes.add(node);

    setup = new Setup();

    workflow = new Workflow();
    workflow.setId(UUID);
    workflow.setSetup(setup);

    processBuilder = new ProcessBuilder(bpmnModelInstance, process);
  }

  @Test
  void testFromWorkflowException() throws JsonProcessingException, ScriptTaskDeserializeCodeFailure {
    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      commonUnmockedProcessBuilder(utility);
      commonMockingsBasic();

      when(objectMapper.writeValueAsString(any())).thenThrow(new MyException(VALUE));

      bpmnModelFactory.fromWorkflow(workflow);
    }
  }

  @Test
  void testFromWorkflowNoNodesWorks() throws ScriptTaskDeserializeCodeFailure {
    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      commonUnmockedProcessBuilder(utility);
      commonMockingsBasic();

      bpmnModelFactory.fromWorkflow(workflow);
    }
  }

  @Test
  void testFromWorkflowProcessBuilderGenericNodeThrowsException() throws JsonProcessingException, ScriptTaskDeserializeCodeFailure {
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

    lenient().when(processBuilderMocked.getElement()).thenReturn(process);
    lenient().when(processBuilderMocked.name(any())).thenReturn(processBuilderMocked);
    lenient().when(processBuilderMocked.camundaHistoryTimeToLive(anyInt())).thenReturn(processBuilderMocked);
    lenient().when(processBuilderMocked.camundaVersionTag(any())).thenReturn(processBuilderMocked);
  }

  /**
   * A helper function for reducing repeated mock code between test functions.
   */
  private void commonMockingsBasic() {
    doNothing().when(process).setCamundaHistoryTimeToLive(anyInt());
    when(bpmnModelInstance.newInstance(ArgumentMatchers.<Class<ModelElementInstance>>any())).thenReturn(extensionElements, camundaField);
    doNothing().when(extensionElements).addChildElement(any());
    when(bpmnModelInstance.getModelElementById(anyString())).thenReturn(modelElementInstance);
  }

  /**
   * Provide an exception that exposes the string initializer for easy usage.
   */
  private class MyException extends JsonProcessingException {

    private static final long serialVersionUID = -6261961424503639802L;

    public MyException(String msg) {
      super(msg);
    }
  }

  /**
   * Provide a non-abstract Node for easy instantiation.
   */
  private class MyNode extends Node {
  }
}
