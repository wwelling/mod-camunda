package org.folio.rest.delegate.iterable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BufferingStreamIterable<T> implements Iterable<List<T>> {

  private final Iterator<T> primary;

  private final int buffer;

  private final long delay;

  public BufferingStreamIterable(Stream<T> primaryStream, int buffer, long delay) {
    this.primary = primaryStream.iterator();
    this.buffer = buffer;
    this.delay = delay;
  }

  @Override
  public Iterator<List<T>> iterator() {
    return new Iterator<List<T>>() {

      @Override
      public boolean hasNext() {
        return primary.hasNext();
      }

      @Override
      public List<T> next() {
        List<T> nodes = new ArrayList<T>();
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

  public Stream<List<T>> toStream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public static <T> BufferingStreamIterable<T> of(Stream<T> primaryStream, int buffer, long delay) {
    return new BufferingStreamIterable<T>(primaryStream, buffer, delay);
  }

}