package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StreamAccumulationDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  private Expression accumulateTo;

  private Expression delayDuration;

  public StreamAccumulationDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    log.info(String.format("%s STARTED", delegateName));

    int buffer = accumulateTo != null ? Integer.parseInt(accumulateTo.getValue(execution).toString()) : 500;
    Long delay = delayDuration != null ? Long.parseLong(delayDuration.getValue(execution).toString()) : 10L;

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    streamService.map(primaryStreamId, buffer, delay, dl -> {
      return String.format("[%s]", String.join(",", dl));
    });

  }

  public void setAccumulateTo(Expression accumulateTo) {
    this.accumulateTo = accumulateTo;
  }

  public void setDelayDuration(Expression delayDuration) {
    this.delayDuration = delayDuration;
  }

}
