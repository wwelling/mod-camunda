package org.folio.rest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import org.folio.rest.delegate.iterable.BufferingStreamIterable;
import org.folio.rest.delegate.iterable.EnhancingStreamIterable;
import org.folio.rest.delegate.iterable.OrderingMergeStreamIterable;
import org.folio.rest.workflow.components.EnhancementComparison;
import org.folio.rest.workflow.components.EnhancementMapping;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StreamService {

  private final ObjectMapper objectMapper;

  private final Map<String, Stream<String>> streams;

  private final Map<String, List<String>> reports;

  public StreamService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    streams = new HashMap<String, Stream<String>>();
    reports = new HashMap<String, List<String>>();
  }

  public Stream<String> getStream(String id) {
    return streams.get(id);
  }

  public void removeStream(String id) {
    streams.remove(id);
  }

  public String concatenateStream(String firstStreamId, Stream<String> secondStream) {
    Stream<String> firstStream = getStream(firstStreamId);
    return setStream(firstStreamId, Stream.concat(firstStream, secondStream));
  }

  public String orderedMergeStream(String firstStreamId, Stream<String> secondStream,
      List<EnhancementComparison> enhancementComparisons) throws JsonParseException, JsonMappingException, IOException {
    Stream<String> firstStream = getStream(firstStreamId);
    Stream<JsonNode> primary = toJsonNodeStream(firstStream);
    Stream<JsonNode> secondary = toJsonNodeStream(secondStream);
    OrderingMergeStreamIterable orderedStreamIterable = OrderingMergeStreamIterable.of(primary, secondary,
        enhancementComparisons);
    Stream<String> result = toStringStream(orderedStreamIterable.toStream());
    return setStream(firstStreamId, result);
  }

  /**
   * Compares two streams of JSON strings using an ordered map of comparison
   * properties and augments the first stream with an enhancement property from
   * the second stream when there is a match. Primary stream and input stream must
   * be sorted by the comparison properties.
   *
   * @param primaryStreamId
   * @param inStream
   * @param comparisonProperties
   * @param enhancementProperty
   * @return primaryStreamId
   * @throws IOException
   */
  public String enhanceStream(String primaryStreamId, Stream<String> inStream,
      List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings)
      throws IOException {
    Stream<String> enhancedStream = enhanceStream(getStream(primaryStreamId), inStream, enhancementComparisons,
        enhancementMappings);
    return setStream(primaryStreamId, enhancedStream);
  }

  public String map(String id, Function<String, String> map) {
    return setStream(id, getStream(id).map(map));
  }

  public String map(String id, int buffer, long delay, Function<List<String>, String> map) {
    BufferingStreamIterable bufferedIterable = BufferingStreamIterable.of(getStream(id), buffer, delay);
    return setStream(id, bufferedIterable.toStream().map(map));
  }

  public String createStream(Stream<String> stream) {
    String id = UUID.randomUUID().toString();
    streams.put(id, stream);
    return id;
  }

  public String setStream(String id, Stream<String> stream) {
    streams.put(id, stream);
    return id;
  }

  public Stream<String> enhanceStream(Stream<String> primaryStream, Stream<String> inStream,
      List<EnhancementComparison> enhancementComparisons, List<EnhancementMapping> enhancementMappings)
      throws IOException {
    Stream<JsonNode> primary = toJsonNodeStream(primaryStream);
    Stream<JsonNode> secondary = toJsonNodeStream(inStream);
    EnhancingStreamIterable enhancedIterable = EnhancingStreamIterable.of(primary, secondary, enhancementComparisons,
        enhancementMappings);
    Stream<JsonNode> result = enhancedIterable.toStream();
    return toStringStream(result);
  }

  public Stream<JsonNode> toJsonNodeStream(Stream<String> stringStream) {
    return stringStream.map(p -> {
      Optional<JsonNode> node = Optional.empty();
      try {
        node = Optional.ofNullable(objectMapper.readTree(p));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return node;
    }).filter(on -> on.isPresent()).map(on -> on.get());
  }

  public Stream<String> toStringStream(Stream<JsonNode> jsonNodeStream) {
    return jsonNodeStream.map(n -> {
      Optional<String> value = Optional.empty();
      try {
        value = Optional.ofNullable(objectMapper.writeValueAsString(n));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
      return value;
    }).filter(v -> v.isPresent()).map(v -> v.get());
  }

  public void appendToReport(String primaryStreamId, String data) {
    if (reports.containsKey(primaryStreamId)) {
      reports.get(primaryStreamId).add(data);
    } else {
      List<String> reportData = new ArrayList<String>();
      reportData.add(data);
      reports.put(primaryStreamId, reportData);
    }
  }

  public List<String> getReport(String primaryStreamId) {
    return reports.get(primaryStreamId);
  }

  public void clearReport(String primaryStreamId) {
    reports.remove(primaryStreamId);
  }
}
