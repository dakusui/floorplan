package com.github.dakusui.floorplan.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

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
    return (T) configurator.resolverFor(attr, policy).apply(attr, configurator, policy);
  }

  public static Action createGroupedAction(
      Context context,
      boolean parallel,
      Function<Component<?>, Component.ActionFactory> actionFactoryCreator,
      Fixture fixture,
      Ref... refs
  ) {
    Action[] actions = Arrays.stream(
        refs
    ).map(
        fixture::lookUp
    ).map(
        actionFactoryCreator::apply
    ).map(
        actionFactory -> actionFactory.apply(context)
    ).collect(
        toList()
    ).toArray(
        new Action[refs.length]
    );
    return parallel ?
        context.concurrent(actions) :
        context.sequential(actions);
  }

  public static void perform(Action action) {
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }

  public static Context newContext() {
    return new Context.Impl();
  }
}
