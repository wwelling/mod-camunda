package org.folio.rest.service;

import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class StreamService {

  private Stream<String> stream;

  public void map(Function<String, String> mapper) {
    stream = stream.map(mapper);
  }

  public Stream<String> getStream() {
    return stream;
  }

  public void setStream(Stream<String> stream) {
    this.stream = stream;
  }

}