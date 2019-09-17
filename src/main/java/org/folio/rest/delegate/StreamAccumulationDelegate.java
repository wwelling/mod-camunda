package org.folio.rest.delegate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StreamAccumulationDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService<String> singleStreamService;

  @Autowired
  private StreamService<List<String>> listStreamService;

  private Expression accumulateTo;

  private Expression delayDuration;

  public StreamAccumulationDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    log.info(String.format("%s STARTED", delegateName));

    Instant start = Instant.now();

    AtomicBoolean finished = new AtomicBoolean();

    int buffer = accumulateTo != null ? Integer.parseInt(accumulateTo.getValue(execution).toString()) : 500;
    int delay = delayDuration != null ? Integer.parseInt(delayDuration.getValue(execution).toString()) : 10;

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    String listStreamId = listStreamService.setFlux(singleStreamService.getFlux(primaryStreamId)
      .buffer(buffer)
      .delayElements(Duration.ofSeconds(delay))
      .doFinally(f->{
        log.info(String.format("\n\nFINISHED STREAM! %s\n\n", f.toString()));
        Instant end = Instant.now();
        log.info("TIME: " + Duration.between(start, end).getSeconds() + " seconds");
        finished.set(true);
      }));

    execution.setVariable("batchStreamId", listStreamId);
  }

  public void setAccumulateTo(Expression accumulateTo) {
    this.accumulateTo = accumulateTo;
  }

  public void setDelayDuration(Expression delayDuration) {
    this.delayDuration = delayDuration;
  }

}
