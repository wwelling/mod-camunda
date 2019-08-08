package org.folio.rest.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

/*
 *  This delegate concatenates a new stream of data to the end of the primary stream
 */
@Service
@Scope("prototype")
public class ConcatenatingExtractorDelegate extends AbstractExtractorDelegate {

  @Autowired
  private StreamService streamService;

  public ConcatenatingExtractorDelegate(WebClient.Builder webClientBuilder) {
    super(webClientBuilder);
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    Flux<String> newStream = this.getStream(execution);

    String primaryStreamId = (String) execution.getVariable("primaryStreamId");
    String newPrimaryStreamId = streamService.concatenateFlux(primaryStreamId, newStream);
    execution.setVariable("primaryStreamId", newPrimaryStreamId);

    log.info("CONCATENATING EXTRACTOR DELEGATE FINISHED");
  }

  public void setStreamSource(Expression streamSource) {
    this.streamSource = streamSource;
  }

}
