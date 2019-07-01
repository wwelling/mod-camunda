package org.folio.rest.delegate;

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.folio.rest.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
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
    webClient = webClientBuilder.baseUrl("https://folio-okapisnapshot.library.tamu.edu").build();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String delegateName = execution.getBpmnModelElementInstance().getName();

    String token = (String) execution.getVariable("okapiToken");
    System.out.println(String.format("%s STARTED", delegateName));

    streamService.getFlux().delayElements(Duration.ofSeconds(10l)).buffer(100).subscribe(rows -> {
      rows.forEach(row -> {
        try {
          webClient
            .post()
            .uri("/organizations-storage/organizations")
            .syncBody(mapper.readTree(row))
            .header("X-Okapi-Tenant", "tern")
            .header("X-Okapi-Token", token)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve().bodyToFlux(String.class)
            .doOnError(err->{
              try {
                Object json = mapper.readValue(row, Object.class);
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                System.out.println(String.format("\n%s: \n", delegateName));
                System.out.println(jsonString);
                System.out.println(err.getMessage());
              } catch (IOException eTwo) {
                eTwo.printStackTrace();
              }
            })
            .subscribe(res -> {
              //System.out.println("SUCCESS");
            });
        } catch (IOException e) {
          e.printStackTrace();
        }

      });
      System.out.println(String.format("\n%s: %s", delegateName, rows.size()));
    });
  }

}
