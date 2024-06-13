package org.folio.rest.camunda.service;

import static org.folio.spring.test.mock.MockMvcConstant.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.folio.rest.workflow.model.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ExtendWith(MockitoExtension.class)
class BpmnModelFactoryTest {

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

    processBuilder = new ProcessBuilder(null, null);
  }

  @Test
  void testFromWorkflowWorks() {
    try (MockedStatic<Bpmn> utility = Mockito.mockStatic(Bpmn.class)) {
      utility.when(() -> Bpmn.createExecutableProcess()).thenReturn(processBuilder);
    }
  }
}
