package org.folio.rest.controller;

import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.exception.WorkflowAlreadyDeactivatedException;
import org.folio.rest.service.CamundaApiService;
import org.folio.rest.workflow.model.Workflow;
import org.folio.spring.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("workflow-engine/workflows")
public class WorkflowController {

  @Autowired
  private CamundaApiService camundaApiService;

  @PostMapping("/activate")
  public Workflow activateWorkflow(@RequestBody Workflow workflow, @TenantHeader String tenant)
      throws WorkflowAlreadyActiveException {
    return camundaApiService.deployWorkflow(workflow, tenant);
  }

  @PostMapping("/deactivate")
  public Workflow deactivateWorkflow(@RequestBody Workflow workflow) throws WorkflowAlreadyDeactivatedException {
    return camundaApiService.undeployWorkflow(workflow);
  }
}
