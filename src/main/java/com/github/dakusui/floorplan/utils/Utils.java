package com.github.dakusui.floorplan.utils;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;

public class Utils {
  ;

  public static <T> Collector<T, List<T>, Optional<T>> singletonCollector() {
    return Collector.of(
        ArrayList::new,
        (ts, t) -> {
          if (ts.isEmpty()) {
            ts.add(t);
            return;
          }
          throw new IllegalStateException();
        },
        (left, right) -> {
          if (left.size() == 1 && right.isEmpty() || left.isEmpty() && right.size() == 1) {
            left.addAll(right);
            return left;
          }
          throw new IllegalStateException();
        },
        list -> {
          if (list.size() > 1) {
            throw new IllegalStateException();
          }
          return list.isEmpty() ?
              Optional.empty() :
              Optional.of(list.get(0));
        }
    );
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute, T> T resolve(A attr, Configurator<A> configurator, Policy policy) {
    return (T)configurator.resolverFor(attr, policy).apply(attr, configurator, policy);
  }
}
