package org.folio.rest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@RunWith(SpringRunner.class)
public class StreamServiceTest {

  private StreamService streamService;

  @Before
  public void setup() {
    streamService = new StreamService(new ObjectMapper());
  }

  @Test
  public void testEnhanceFlux() throws IOException {
    String id = "primaryStream";

    Flux<String> primary = Flux.fromIterable(createPrimary(1000000));

    Flux<String> secondary = Flux.fromIterable(createSecondary(1000000));

    String comparisonProperties = "{\"id\":\"id\",\"schema\":\"schema\"}";

    String enhancementProperty = "netid";

    streamService.setFlux(id, primary);

    long startTime = System.nanoTime();
    streamService.enhanceFlux(id, secondary, comparisonProperties, enhancementProperty);

    streamService.getFlux(id).subscribe(tuple -> {
      // System.out.println("\t" + tuple);
    });

    long endTime = System.nanoTime();

    long duration = (endTime - startTime);

    System.out.println("\n\n" + (duration / 1000000) + " milliseconds\n\n");

  }

  @Test
  public void testEnhanceFlux2() throws IOException {
    String id = "primaryStream";

    Flux<String> primary = Flux.fromIterable(createPrimary(1000000));

    Flux<String> secondary = Flux.fromIterable(createSecondary(1000000));

    String comparisonProperties = "{\"id\":\"id\",\"schema\":\"schema\"}";

    String enhancementProperty = "netid";

    streamService.setFlux(id, primary);

    long startTime = System.nanoTime();
    streamService.enhanceFlux2(id, secondary, comparisonProperties, enhancementProperty);

    streamService.getFlux(id).subscribe(tuple -> {
      // System.out.println("\t" + tuple);
    });

    long endTime = System.nanoTime();

    long duration = (endTime - startTime);

    System.out.println("\n\n" + (duration / 1000000) + " milliseconds\n\n");

  }

  private List<String> createPrimary(int count) {
    List<String> stream = new ArrayList<String>();
    for (int i = 0; i < count; i++) {
      String schema = i % 2 == 0 ? "AMDB" : "MSDB";
      String row = String.format("{\"id\":\"%s\",\"schema\":\"%s\",\"firstName\":\"Test\",\"lastName\":\"User\"}", i, schema);
      stream.add(row);
    }
    return stream;
  }

  private List<String> createSecondary(int count) {
    List<String> stream = new ArrayList<String>();
    for (int i = 0; i < count; i += 3) {
      String schema = i % 2 == 0 ? "AMDB" : "MSDB";
      String row = String.format("{\"id\":\"%s\",\"schema\":\"%s\",\"netid\":\"%s\"}", i, schema, i);
      stream.add(row);
    }
    return stream;
  }

}
