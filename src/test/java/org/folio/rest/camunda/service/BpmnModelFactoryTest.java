package org.folio.rest.camunda.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.folio.rest.camunda.delegate.AbstractWorkflowDelegate;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
import org.folio.rest.workflow.enums.StartEventType;
import org.folio.rest.workflow.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class BpmnModelFactoryTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private List<AbstractWorkflowDelegate> workflowDelegates;

    @InjectMocks
    private BpmnModelFactory bpmnModelFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // @Test
    // void testFromWorkflow() throws ScriptTaskDeserializeCodeFailure, NoSuchFieldException, IllegalAccessException, JsonProcessingException {
    //     // Setup mock Workflow object
    //     Workflow workflow = mock(Workflow.class);
    //     when(workflow.getName()).thenReturn("Test Workflow");
    //     when(workflow.getHistoryTimeToLive()).thenReturn(10);
    //     when(workflow.getVersionTag()).thenReturn("v1.0");
    //     when(workflow.getNodes()).thenReturn(Collections.emptyList());
    //     when(workflow.getInitialContext()).thenReturn(Collections.emptyMap());

    //     // Print the mocked data directly
    //     System.out.println("Workflow Name: " + workflow.getName());
    //     System.out.println("History Time To Live: " + workflow.getHistoryTimeToLive());
    //     System.out.println("Version Tag: " + workflow.getVersionTag());
    //     System.out.println("Nodes: " + workflow.getNodes());
    //     System.out.println("Initial Context: " + workflow.getInitialContext());

    //     // Mock BpmnModelInstance and builders
    //     BpmnModelInstance modelInstance = mock(BpmnModelInstance.class);
    //     ProcessBuilder processBuilder = mock(ProcessBuilder.class);
    //     StartEventBuilder startEventBuilder = mock(StartEventBuilder.class);

    //     // Ensure each builder method returns the appropriate mock object
    //     when(processBuilder.name(anyString())).thenReturn(processBuilder);
    //     when(processBuilder.camundaHistoryTimeToLive(anyInt())).thenReturn(processBuilder);
    //     when(processBuilder.camundaVersionTag(anyString())).thenReturn(processBuilder);
    //     when(processBuilder.startEvent()).thenReturn(startEventBuilder);
    //     when(processBuilder.done()).thenReturn(modelInstance);
    //     when(startEventBuilder.done()).thenReturn(modelInstance);

    //     System.out.println("ProcessBuilder: " + processBuilder);
    //     // Print the mocked data directly
    //     System.out.println("Workflow Name: " + workflow.getName());
    //     System.out.println("History Time To Live: " + workflow.getHistoryTimeToLive());
    //     System.out.println("Version Tag: " + workflow.getVersionTag());
    //     System.out.println("Nodes: " + workflow.getNodes());
    //     System.out.println("Initial Context: " + workflow.getInitialContext());

    //     ModelElementInstance element = mock(ModelElementInstance.class);
    //     when(modelInstance.getModelElementById(anyString())).thenReturn(element);

    //     // Use reflection to set private fields
    //     BpmnModelFactory factory = new BpmnModelFactory();

    //     Field objectMapperField = BpmnModelFactory.class.getDeclaredField("objectMapper");
    //     objectMapperField.setAccessible(true);
    //     objectMapperField.set(factory, objectMapper);

    //     Field workflowDelegatesField = BpmnModelFactory.class.getDeclaredField("workflowDelegates");
    //     workflowDelegatesField.setAccessible(true);
    //     workflowDelegatesField.set(factory, workflowDelegates);

    //     // Mock Bpmn static method
    //     try (MockedStatic<Bpmn> mockedBpmn = mockStatic(Bpmn.class)) {
    //       mockedBpmn.when(Bpmn::createExecutableProcess).thenReturn(processBuilder);

    //       BpmnModelInstance result = factory.fromWorkflow(workflow);

    //       // Ensure the expected methods are called
    //       verify(workflow, times(1)).getNodes();
    //       verify(workflow, times(1)).getInitialContext();
          
    //       // Print the resulting model instance for debugging
    //       System.out.println("Resulting BpmnModelInstance: " + result);

    //       assertNotNull(result);
    //   }
    // }

    @Test
    void testFromWorkflowWithException() throws JsonProcessingException {
        Workflow workflow = mock(Workflow.class);
        when(workflow.getName()).thenReturn("Test Workflow");
        when(workflow.getHistoryTimeToLive()).thenReturn(100);
        when(workflow.getVersionTag()).thenReturn("v1.0");

        StartEvent startEvent = mock(StartEvent.class);
        when(startEvent.getType()).thenReturn(StartEventType.MESSAGE_CORRELATION);
        when(startEvent.getExpression()).thenReturn(null);
        when(startEvent.isAsyncBefore()).thenReturn(false);
        when(startEvent.isInterrupting()).thenReturn(true);
        lenient().when(startEvent.getIdentifier()).thenReturn("startEvent");
        lenient().when(startEvent.getName()).thenReturn("Start Event");

        when(workflow.getNodes()).thenReturn(Collections.singletonList(startEvent));
        when(workflow.getSetup()).thenReturn(new org.folio.rest.workflow.model.Setup());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bpmnModelFactory.fromWorkflow(workflow);
        });

        assertEquals("MESSAGE_CORRELATION start event requests an expression", exception.getMessage());
    }

    @Test
    void testBuildWithEmptyNodes() throws Exception {
        Workflow workflow = mock(Workflow.class);
        when(workflow.getNodes()).thenReturn(Collections.emptyList());

        BpmnModelFactory factory = new BpmnModelFactory();

        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        when(processBuilder.done()).thenReturn(mock(BpmnModelInstance.class));

        Method buildMethod = BpmnModelFactory.class.getDeclaredMethod("build", ProcessBuilder.class, Workflow.class);
        buildMethod.setAccessible(true);

        BpmnModelInstance modelInstance = (BpmnModelInstance) buildMethod.invoke(factory, processBuilder, workflow);

        assertNotNull(modelInstance);
    }

    @Test
    void testBuildWithNodes() throws Exception {
        // Create a mock Workflow object
        Workflow workflow = mock(Workflow.class);

        // Create a mock StartEvent object
        StartEvent startEvent = mock(StartEvent.class);
        when(startEvent.getType()).thenReturn(StartEventType.NONE);
        when(startEvent.isAsyncBefore()).thenReturn(false);
        when(startEvent.isInterrupting()).thenReturn(true);
        when(startEvent.getIdentifier()).thenReturn("startEvent");
        when(startEvent.getName()).thenReturn("Start Event");

        // Assume there is a method to set up the setup, if it's not getSetup
        Setup setup = mock(Setup.class);
        // when(setup.isAsyncAfter()).thenReturn(false);
        // doReturn(setup).when(startEvent).getSetup();

        // Add the mock StartEvent to a list of nodes
        List<Node> nodes = new ArrayList<>();
        nodes.add(startEvent);
        when(workflow.getNodes()).thenReturn(nodes);

        // Create an instance of BpmnModelFactory
        BpmnModelFactory factory = new BpmnModelFactory();

        // Create a mock ProcessBuilder object
        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        StartEventBuilder startEventBuilder = mock(StartEventBuilder.class);
        when(processBuilder.startEvent()).thenReturn(startEventBuilder);
        when(startEventBuilder.done()).thenReturn(mock(BpmnModelInstance.class));

        // Use reflection to access the private build method
        Method buildMethod = BpmnModelFactory.class.getDeclaredMethod("build", ProcessBuilder.class, Workflow.class);
        buildMethod.setAccessible(true);

        // Invoke the build method using reflection
        BpmnModelInstance modelInstance = (BpmnModelInstance) buildMethod.invoke(factory, processBuilder, workflow);

        // Assert that the returned BpmnModelInstance is not null
        assertNotNull(modelInstance);
    }
}
