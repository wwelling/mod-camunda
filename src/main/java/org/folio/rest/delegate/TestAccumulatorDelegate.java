package org.folio.rest.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Scope("prototype")
public class TestAccumulatorDelegate extends AbstractRuntimeDelegate {

  @Autowired
  private StreamService streamService;

  @Autowired
  private ObjectMapper mapper;

  private final WebClient webClient;

  public TestAccumulatorDelegate(WebClient.Builder webClientBuilder) {
    super();
    webClient = webClientBuilder.baseUrl("http://localhost:9130").build();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String token = (String) execution.getVariable("okapiToken");
    System.out.println(String.format("%s STARTED", delegateName));
    streamService.getFlux().buffer(500).subscribe(rows -> {
      rows.forEach(row -> {
        System.out.println(String.format("\n%s: %s", delegateName, row));
        // try {
          // webClient
          //   .post()
          //   .uri("/organizations-storage/organizations")
          //   .syncBody(mapper.readTree(row))
          //   .header("X-Okapi-Tenant", "diku")
          //   .header("X-Okapi-Token", token)
          //   .accept(MediaType.APPLICATION_JSON)
          //   .retrieve().bodyToFlux(String.class).subscribe(res -> {
          //     //System.out.println(res);
          //   });
        // } catch (IOException e) {
        //   e.printStackTrace();
        // }
      });
      System.out.println(String.format("\n%s: %s", delegateName, rows.size()));
    });
  }

}
