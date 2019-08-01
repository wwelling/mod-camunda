package org.folio.rest.controller;

import org.folio.rest.annotation.TokenHeader;
import org.folio.rest.exception.UnableToActivateWorkflowException;
import org.folio.rest.exception.WorkflowAlreadyActiveException;
import org.folio.rest.model.Workflow;
import org.folio.rest.service.CamundaApiService;
import org.folio.rest.tenant.annotation.TenantHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflows")
public class WorkflowController {

  @Autowired
  private CamundaApiService camundaApiService;

  @PostMapping("/{id}/activate")
  public Workflow activateWorkflow(
    @TenantHeader String tenant,
    @TokenHeader String token,
    @PathVariable String id,
    @RequestBody Workflow workflow
  ) throws WorkflowAlreadyActiveException, UnableToActivateWorkflowException {

    return camundaApiService.deployWorkflow(workflow, tenant, token, id);
  }
}
