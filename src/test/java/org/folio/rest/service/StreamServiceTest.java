package org.folio.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.folio.rest.workflow.components.EnhancementComparison;
import org.folio.rest.workflow.components.EnhancementMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
public class StreamServiceTest {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private StreamService streamService;

  @Before
  public void setup() {
    streamService = new StreamService(new ObjectMapper());
  }

  @Test
  public void testEnhanceStream() throws IOException {
    int count = 10000;

    Stream<String> primary = createPrimary(count).stream();

    Stream<String> secondary = createSecondary(count).stream();

    List<EnhancementComparison> enhancementComparisons = new ArrayList<EnhancementComparison>();
    enhancementComparisons.add(new EnhancementComparison("/id", "/id"));
    enhancementComparisons.add(new EnhancementComparison("/schema", "/type.name"));
    List<EnhancementMapping> enhancementMappings =  new ArrayList<EnhancementMapping>();
    enhancementMappings.add(new EnhancementMapping("netid", "/netid"));

    long startTime = System.nanoTime();
    Stream<String> enhancedStream = streamService.enhanceStream(primary, secondary, enhancementComparisons, enhancementMappings);

    List<String> excpected = createExpected(count);

    AtomicInteger index = new AtomicInteger(0);

    enhancedStream.forEach(row -> {
      assertEquals(excpected.get(index.getAndIncrement()), row);
    });
    
    assertEquals(index.get(), count);

    long endTime = System.nanoTime();

    long duration = (endTime - startTime);

    logger.info(String.format("Took %s milliseconds to enhance stream of %s rows", (duration / 1000000), count));
  }

  @Test
  public void testToJsonNodeStream() {
    Stream<String> initialStream = createPrimary(10).stream();
    streamService.toJsonNodeStream(initialStream).forEach(node -> {
      assertTrue(node instanceof JsonNode);
    });
  }

  @Test
  public void testToStringStream() {
    Stream<String> initialStream = createPrimary(10).stream();
    Stream<JsonNode> jsonNodeStream = streamService.toJsonNodeStream(initialStream);
    Stream<String> stringStream = streamService.toStringStream(jsonNodeStream);
    stringStream.forEach(node -> {
      assertTrue(node instanceof String);
    });
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
      String row = String.format("{\"id\":\"%s\",\"type.name\":\"%s\",\"netid\":\"%s\"}", i, schema, i);
      stream.add(row);
    }
    return stream;
  }

  private List<String> createExpected(int count) {
    List<String> stream = new ArrayList<String>();
    for (int i = 0; i < count; i++) {
      String schema = i % 2 == 0 ? "AMDB" : "MSDB";

      String row;
      if (i % 3 == 0) {
        row = String.format("{\"id\":\"%s\",\"schema\":\"%s\",\"firstName\":\"Test\",\"lastName\":\"User\",\"netid\":\"%s\"}", i, schema, i);
      } else {
        row = String.format("{\"id\":\"%s\",\"schema\":\"%s\",\"firstName\":\"Test\",\"lastName\":\"User\"}", i, schema);
      }

      stream.add(row);
    }
    return stream;
  }

}
