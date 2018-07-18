package com.github.dakusui.floorplan.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;
import static org.junit.Assume.assumeThat;

public class InternalUtils {
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
          require(list, l -> l.size() <= 1, l -> IllegalArgumentException::new);
          return list.isEmpty() ?
              Optional.empty() :
              Optional.of(list.get(0));
        }
    );
  }

  public static void performAction(Action action) {
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }

  public static Context newContext() {
    return new Context.Impl();
  }

  public static <T, R> Function<T, R> toPrintableFunction(Supplier<String> messageSupplier, Function<T, R> func) {
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

  public static <T> Predicate<T> toPrintablePredicate(Supplier<String> messageSupplier, Predicate<T> pred) {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return pred.test(t);
      }

      @Override
      public Predicate<T> negate() {
        return toPrintablePredicate(() -> String.format("!%s", this.toString()), pred.negate());
      }

      @Override
      public Predicate<T> and(Predicate<? super T> another) {
        return toPrintablePredicate(() -> String.format("and(%s,%s)", this.toString(), another.toString()), pred.and(another));
      }

      @Override
      public Predicate<T> or(Predicate<? super T> another) {
        return toPrintablePredicate(() -> String.format("or(%s,%s)", this.toString(), another.toString()), pred.or(another));
      }


      @Override
      public String toString() {
        return messageSupplier.get();
      }
    };
  }

  public static Predicate<Object> isInstanceOf(Class<?> expectedType) {
    return InternalUtils.toPrintablePredicate(
        () -> String.format("assignableTo[%s]", expectedType.getSimpleName()),
        expectedType.isPrimitive() ?
            v -> v != null && expectedType.isAssignableFrom(v.getClass()) :
            v -> v == null || expectedType.isAssignableFrom(v.getClass())
    );
  }

  public static <A extends Attribute> Predicate<Object> hasSpecOf(ComponentSpec<A> spec) {
    return InternalUtils.toPrintablePredicate(
        () -> String.format("hasSpecOf[%s]", spec),
        (Object v) -> Objects.equals((Ref.class.cast(v)).spec(), spec)
    );
  }

  @SuppressWarnings({ "unchecked" })
  public static Predicate<Object> forAll(Predicate<Object> pred) {
    return InternalUtils.toPrintablePredicate(
        () -> String.format("allMatch[%s]", pred),
        (Object v) -> List.class.cast(v).stream().allMatch(pred)
    );
  }

  @SuppressWarnings("unchecked")
  public static <A> A getStaticFieldValue(Field field) {
    try {
      boolean wasAccessible = field.isAccessible();
      field.setAccessible(true);
      try {
        return (A) field.get(null);
      } finally {
        field.setAccessible(wasAccessible);
      }
    } catch (IllegalAccessException e) {
      throw Exceptions.rethrow(e);
    }
  }

  public static <T> T createWithNoParameterConstructor(Class<T> tClass) {
    try {
      return requireNonNull(tClass).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.rethrow(e);
    }
  }

}
