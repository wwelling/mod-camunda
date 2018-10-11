package org.folio.rest.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.tenant.config.TenantConfig;
import org.folio.rest.tenant.exception.NoTenantException;
import org.folio.rest.tenant.storage.ThreadLocalStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantInjectionDelegateAspect {

  @Autowired
  private TenantConfig tenantConfig;

  @Before("execution(* org.camunda.bpm.engine.delegate.JavaDelegate.execute (org.camunda.bpm.engine.delegate.DelegateExecution)) && args(execution)")
  public void beforeDelegateExecution(DelegateExecution execution) {
    String tenant = execution.getTenantId();
    if (tenant == null) {
      // NOTE: force tenant is also used to enforce HTTP requests to contain tenant header
      if (tenantConfig.isForceTenant()) {
        throw new NoTenantException("No tenant found in thread safe tenant storage!");
      }
      tenant = tenantConfig.getDefaultTenant();
    }
    ThreadLocalStorage.setTenant(tenant);
  }

}
