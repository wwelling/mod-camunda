package org.folio.rest.delegate;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.Request;
import org.folio.rest.workflow.model.Stream;
import org.folio.rest.workflow.model.StreamingExtractTransformLoadTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;

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

    List<Stream> streams = objectMapper.readValue(this.streams.getValue(execution).toString(),
        new TypeReference<List<Stream>>() {
        });

    List<Process> processes = objectMapper.readValue(this.processes.getValue(execution).toString(),
        new TypeReference<List<Process>>() {
        });

    List<Request> requests = objectMapper.readValue(this.requests.getValue(execution).toString(),
        new TypeReference<List<Request>>() {
        });

    Map<String, Object> input = getInputs(execution);

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