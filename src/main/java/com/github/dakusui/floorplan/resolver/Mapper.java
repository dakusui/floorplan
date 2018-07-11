package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Mapper<A extends Attribute, T, R> extends Function<A, Function<Configurator<A>, Function<Policy, Function<T, R>>>> {
  default R apply(A attr, Configurator<A> configurator, Policy policy, T value) {
    return this.apply(attr).apply(configurator).apply(policy).apply(value);
  }

  static <A extends Attribute, T, R> Mapper<A, T, R> of(Function<A, Function<Configurator<A>, Function<Policy, Function<T, R>>>> func) {
    return of(func, () -> "Mapper(noname)");
  }

  static <A extends Attribute, T, R> Mapper<A, T, R> of(Function<A, Function<Configurator<A>, Function<Policy, Function<T, R>>>> func, Supplier<String> messageSupplier) {
    return new Mapper<A, T, R>() {
      @Override
      public Function<Configurator<A>, Function<Policy, Function<T, R>>> apply(A a) {
        return c -> p -> t -> func.apply(a).apply(c).apply(p).apply(t);
      }

      @Override
      public String toString() {
        return messageSupplier.get();
      }
    };
  }

  static <A extends Attribute, T, R> Mapper<A, T, R> create(Function<T, R> func) {
    return of(a -> c -> p -> func);
  }
}
