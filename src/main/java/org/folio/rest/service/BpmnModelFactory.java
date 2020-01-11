package org.folio.rest.service;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.folio.rest.workflow.components.Workflow;
import org.springframework.stereotype.Service;

@Service
public class BpmnModelFactory {

  public BpmnModelInstance fromWorkflow(Workflow workflow) {
    BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

    return modelInstance;
  }

}
