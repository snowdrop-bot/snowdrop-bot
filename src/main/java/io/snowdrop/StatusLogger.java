package io.snowdrop;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.smallrye.reactive.messaging.annotations.Emitter;

public class StatusLogger {

  private final String category;
  private final Emitter<Status> emitter;

  public StatusLogger(String category, Emitter<Status> emitter) {
    this.category = category;
    this.emitter = emitter;
  }

  public <T> Function<T,T> log(long total, String format, Function<T, Object>... args) {
    final AtomicInteger  counter = new AtomicInteger();
    return t -> {
      String message = args.length == 0
        ? String.format(format, t)
        : String.format(format, Arrays.stream(args).map(e -> e.apply(t)).collect(Collectors.toList()).toArray());
      try {
        emitter.send(new Status(category, 100 * counter.incrementAndGet() / total, message));
      } catch (Exception e) {
        // if the emitter is not `connected` an error will be thrown.
      }
      return t;
    };
  }
}
