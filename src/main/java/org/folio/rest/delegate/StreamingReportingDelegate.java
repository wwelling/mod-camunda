package org.folio.rest.delegate;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StreamingReportingDelegate extends AbstractRuntimeDelegate {
  @Autowired
  private StreamService streamService;

  public StreamingReportingDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    log.info("{} STARTED", delegateName);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    AtomicInteger counter = new AtomicInteger(1);

    streamService.setFlux(primaryStreamId, streamService.getFlux(primaryStreamId)
      .doFinally(r -> {
        log.info("Building Report at {}",Instant.now());

        streamService.getReport(primaryStreamId).forEach(e -> {
          log.info("Entry "+counter+": "+e);
          counter.getAndIncrement();
        });
        streamService.clearReport(primaryStreamId);
      }));
  }
}