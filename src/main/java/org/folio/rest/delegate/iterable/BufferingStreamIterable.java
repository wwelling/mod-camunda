package org.folio.rest.delegate.iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BufferingStreamIterable implements Iterable<List<String>> {

  private final Iterator<String> primary;

  private final int buffer;

  private final long delay;

  public BufferingStreamIterable(Stream<String> primaryStream, int buffer, long delay) {
    this.primary = primaryStream.iterator();
    this.buffer = buffer;
    this.delay = delay;
  }

  @Override
  public Iterator<List<String>> iterator() {
    return new Iterator<List<String>>() {

      @Override
      public boolean hasNext() {
        return primary.hasNext();
      }

      @Override
      public List<String> next() {
        List<String> nodes = new ArrayList<String>();
        int count = 0;
        while (count++ < buffer && primary.hasNext()) {
          nodes.add(primary.next());
        }
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return nodes;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  public Stream<List<String>> toStream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public static BufferingStreamIterable of(Stream<String> primaryStream, int buffer, long delay) {
    return new BufferingStreamIterable(primaryStream, buffer, delay);
  }

}