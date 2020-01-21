package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.StreamingExtractTransformLoadTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Scope("prototype")
public class StreamExtractTransformLoadDelegate extends AbstractWorkflowIODelegate {

  @Autowired
  private WebClient webClient;

  private Expression streams;

  private Expression processes;

  private Expression requests;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();

    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();

    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

  }

  public void setStreams(Expression streams) {
    this.streams = streams;
  }

  public void setProcesses(Expression processes) {
    this.processes = processes;
  }

  public void setRequests(Expression requests) {
    this.requests = requests;
  }

  @Override
  public Class<?> fromTask() {
    return StreamingExtractTransformLoadTask.class;
  }

}