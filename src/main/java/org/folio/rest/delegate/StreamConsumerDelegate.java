package org.folio.rest.delegate;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class StreamConsumerDelegate extends AbstractReportableDelegate {

  @Autowired
  private StreamService streamService;

  public StreamConsumerDelegate() {
    super();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    log.info("{} STARTED", delegateName);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");

    // NOTE: could use streamService.toJsonNodeStream if need JsonNode in iteration
    streamService.getStream(primaryStreamId).forEach(r -> {
      System.out.println("\n" + r + "\n");
    });

    log.info("Stream consumption completed");

    streamService.removeStream(primaryStreamId);

    if (isReporting()) {
      AtomicInteger counter = new AtomicInteger(1);

      log.info("Building Report at {}", Instant.now());

      streamService.getReport(primaryStreamId).forEach(e -> {
        log.info("Entry " + counter + ": " + e);
        counter.getAndIncrement();
      });
      streamService.removeReport(primaryStreamId);
    }

  }

}