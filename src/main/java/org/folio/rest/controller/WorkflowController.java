package org.folio.rest.controller;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.folio.rest.annotation.TokenHeader;
import org.folio.rest.tenant.annotation.TenantHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflows")
public class WorkflowController {

  @PostMapping("/{id}/activate")
  public JsonInclude activateWorkflow(
    @TenantHeader String tenant,
    @TokenHeader String token,
    @PathVariable String id,
    @RequestBody Workflow workflow) {

    if (workflow.isPresent()) {
    // return modCamundaService.deployWorkflow(tenant, token, workflow.get());
    }
    // throw new WorkflowNotFoundException(id);
  }
}
