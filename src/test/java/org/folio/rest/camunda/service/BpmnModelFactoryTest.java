package org.folio.rest.camunda.service;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.folio.spring.test.mock.MockMvcConstant.VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResults;
import org.folio.rest.camunda.exception.ScriptTaskDeserializeCodeFailure;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
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

  @SpyBean
  private ObjectMapper objectMapper;

  @InjectMocks
  private BpmnModelFactory bpmnModelFactory;

  private ProcessBuilder processBuilder;

  private Workflow workflow;

  @BeforeEach
  void beforeEach() {
    workflow = new Workflow();
    workflow.setId(UUID);

    processBuilder = new ProcessBuilder(bpmnModelInstance, process);
  }

  @SuppressWarnings("deprecation")
  @Test
  void testFromWorkflowExceptionWorks() throws JsonProcessingException, ScriptTaskDeserializeCodeFailure {
    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      utility.when(() -> Bpmn.createExecutableProcess()).thenReturn(processBuilder);

      doNothing().when(process).setCamundaHistoryTimeToLive(anyInt());
      when(bpmnModelInstance.newInstance(ArgumentMatchers.<Class<ModelElementInstance>>any())).thenReturn(extensionElements, camundaField);
      when(objectMapper.writeValueAsString(any())).thenThrow(new MyException(VALUE));
      doNothing().when(extensionElements).addChildElement(any());
      when(bpmnModelInstance.getModelElementById(anyString())).thenReturn(modelElementInstance);

      bpmnModelFactory.fromWorkflow(workflow);
    }
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
}
