package org.folio.rest.camunda.aspect;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class DelegateExecutionExceptionAspectTest {

  @Mock
  private JavaMailSender emailSender;

  @Mock
  private DelegateExecution execution;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private ProcessEngineServices processEngineServices;

  @Mock
  private ProcessDefinitionQuery processDefinitionQuery;

  @Mock
  private ProcessDefinition processDefinition;

  @Mock
  private StackTraceElement stackTraceElement;

  @Mock
  private Throwable exception;

  @InjectMocks
  private DelegateExecutionExceptionAspect delegateExecutionExceptionAspect;

  @Test
  void testAfterDelegateExecutionThrowsException() {
    ReflectionTestUtils.setField(delegateExecutionExceptionAspect, "errorHandlingEnvironment", "DEV");
    ReflectionTestUtils.setField(delegateExecutionExceptionAspect, "errorHandlingEmailFrom", "helpdesk@library.tamu.edu");
    ReflectionTestUtils.setField(delegateExecutionExceptionAspect, "errorHandlingEmailTo", "helpdesk@library.tamu.edu");

    when(execution.getCurrentActivityName()).thenReturn("taskName");
    when(execution.getTenantId()).thenReturn("tenant");
    when(execution.getTenantId()).thenReturn("tenant");
    when(execution.getProcessDefinitionId()).thenReturn("id");
    when(processDefinition.getName()).thenReturn("workflowName");
    when(processDefinitionQuery.singleResult()).thenReturn(processDefinition);

    when(processDefinitionQuery.processDefinitionId(eq(execution.getProcessDefinitionId()))).thenReturn(processDefinitionQuery);

    when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
    when(processEngineServices.getRepositoryService()).thenReturn(repositoryService);
    when(execution.getProcessEngineServices()).thenReturn(processEngineServices);

    when(exception.getStackTrace()).thenReturn(new StackTraceElement[] { stackTraceElement });
    when(stackTraceElement.toString()).thenReturn("");

    doNothing().when(emailSender).send(any(SimpleMailMessage.class));

    delegateExecutionExceptionAspect.afterDelegateExecutionThrowsException(execution, exception);

    verify(emailSender).send(any(SimpleMailMessage.class));
  }

}
