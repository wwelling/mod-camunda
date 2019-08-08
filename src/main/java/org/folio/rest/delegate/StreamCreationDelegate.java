package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Flux;

public class StreamCreationDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Flux<String> primaryStream = Flux.empty();
    String primaryStreamId = streamService.setFlux(primaryStream);
    execution.setVariable("primaryStreamId", primaryStreamId);

    log.info("CREATED PRIMARY STREAM");
  }

}