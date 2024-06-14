package org.folio.rest.camunda.aspect;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.folio.spring.tenant.exception.NoTenantException;
import org.folio.spring.tenant.properties.TenantProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class TenantInjectionDelegateAspectTest {

  @Mock
  private DelegateExecution execution;

  @Mock
  private DelegateTask task;

  @Mock
  private TenantProperties tenantProperties;

  @InjectMocks
  private TenantInjectionDelegateAspect tenantInjectionDelegateAspect;

  @BeforeEach
  void mockDefaultTenant() {
    lenient().when(execution.getTenantId()).thenReturn("diku");
    lenient().when(task.getTenantId()).thenReturn("diku");
    lenient().when(tenantProperties.getDefaultTenant()).thenReturn("diku");
    lenient().when(tenantProperties.isForceTenant()).thenReturn(false);
  }

  @Test
  void testBeforeDelegateExecution() {
    tenantInjectionDelegateAspect.beforeDelegateExecution(execution);

    Mockito.verify(execution).getTenantId();


    lenient().when(execution.getTenantId()).thenReturn(null);
    lenient().when(tenantProperties.isForceTenant()).thenReturn(true);
    assertThrows(NoTenantException.class, () -> tenantInjectionDelegateAspect.beforeDelegateExecution(execution));
  }

  @Test
  void testBeforeExecutionListenerNotify() {
    tenantInjectionDelegateAspect.beforeExecutionListenerNotify(execution);

    Mockito.verify(execution).getTenantId();


    lenient().when(execution.getTenantId()).thenReturn(null);
    lenient().when(tenantProperties.isForceTenant()).thenReturn(true);
    assertThrows(NoTenantException.class, () -> tenantInjectionDelegateAspect.beforeExecutionListenerNotify(execution));
  }

  @Test
  void testBeforeTaskListenerNotify() {
    tenantInjectionDelegateAspect.beforeTaskListenerNotify(task);

    Mockito.verify(task).getTenantId();


    lenient().when(execution.getTenantId()).thenReturn(null);
    lenient().when(task.getTenantId()).thenReturn(null);
    lenient().when(tenantProperties.isForceTenant()).thenReturn(true);
    assertThrows(NoTenantException.class, () -> tenantInjectionDelegateAspect.beforeTaskListenerNotify(task));
  }

}
