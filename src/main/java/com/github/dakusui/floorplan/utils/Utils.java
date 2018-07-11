package com.github.dakusui.floorplan.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

  public static void printAction(Action action) {
    ReportingActionPerformer performer = new ReportingActionPerformer.Builder(action).build();
    performer.report();
    performer.performAndReport();
  }

  public static void performAction(Action action) {
    ReportingActionPerformer performer = new ReportingActionPerformer.Builder(action).build();
    performer.report();
    performer.performAndReport();
  }

  public static Context newContext() {
    return new Context.Impl();
  }

  public static <T, R> Function<T, R> toPrintable(Supplier<String> messageSupplier, Function<T, R> func) {
    return new Function<T, R>() {
      @Override
      public R apply(T t) {
        return func.apply(t);
      }

      @Override
      public String toString() {
        return messageSupplier.get();
      }
    };
  }

  public static <T> Predicate<T> toPrintable(Supplier<String> messageSupplier, Predicate<T> pred) {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return pred.test(t);
      }

      @Override
      public Predicate<T> negate() {
        return toPrintable(() -> String.format("!%s", this.toString()), pred.negate());
      }

      @Override
      public Predicate<T> and(Predicate<? super T> another) {
        return toPrintable(() -> String.format("and(%s,%s)", this.toString(), another.toString()), pred.and(another));
      }

      @Override
      public Predicate<T> or(Predicate<? super T> another) {
        return toPrintable(() -> String.format("or(%s,%s)", this.toString(), another.toString()), pred.or(another));
      }


      @Override
      public String toString() {
        return messageSupplier.get();
      }
    };
  }

  public static Predicate<Object> isInstanceOf(Class<?> expectedType) {
    return Utils.<Object>toPrintable(
        () -> String.format("assignableTo[%s]", expectedType.getSimpleName()),
        expectedType.isPrimitive() ?
            v -> v != null && expectedType.isAssignableFrom(v.getClass()) :
            v -> v == null || expectedType.isAssignableFrom(v.getClass())
    );
  }

  public static <A extends Attribute> Predicate<Object> hasSpecOf(ComponentSpec<A> spec) {
    return Utils.toPrintable(
        () -> String.format("hasSpecOf[%s]", spec),
        (Object v) -> Objects.equals((Ref.class.cast(v)).spec(), spec)
    );
  }

  @SuppressWarnings("PointlessBooleanExpression")
  public static Predicate<Object> forAll(Predicate<Object> pred) {
    return Utils.toPrintable(
        () -> String.format("allMatch[%s]", pred),
        (Object v) -> List.class.cast(v).stream().allMatch(pred) == true
    );
  }
}
