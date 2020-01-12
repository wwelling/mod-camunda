package org.folio.rest.service;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.workflow.components.Workflow;
import org.springframework.stereotype.Service;

@Service
public class BpmnModelFactory {

  public BpmnModelInstance fromWorkflow(Workflow workflow) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess()
      .name(workflow.getName())
      .startEvent()
        .name("Inventory Reference Link Workflow Trigger")
        .message("/events/referencelink/voyager/inventory")
      .serviceTask(String.format("t_%s", "extract"))
        .name("Extract")
        .camundaDelegateExpression(String.format("${%s}", "requestDelegate"))
      .endEvent()
      .done();

    return modelInstance;
  }

}
