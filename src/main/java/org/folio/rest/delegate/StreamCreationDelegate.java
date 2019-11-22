package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class StreamCreationDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  private Expression isReporting;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Flux<String> primaryStream = Flux.empty();
    String primaryStreamId = streamService.createFlux(primaryStream);
    Boolean isReportingValue = (isReporting) != null ? Boolean.parseBoolean(isReporting.getValue(execution).toString()) : false;

    execution.setVariable("primaryStreamId", primaryStreamId);
    execution.setVariable("isReporting", isReportingValue);

    if (isReportingValue) {
      log.info("Reporting enabled");
      streamService.appendToReport(primaryStreamId, String.format("Beginning Streaming Report at %s", Instant.now()));
    } else {
      log.info("Reporting disabled");
    }
    log.info("CREATED PRIMARY STREAM");
  }

  public void setIsReporting(Expression isReporting) {
    this.isReporting = isReporting;
  }

}