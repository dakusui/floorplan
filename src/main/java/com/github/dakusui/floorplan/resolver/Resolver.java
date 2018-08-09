package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Resolver<A extends Attribute, T> extends Function<Configurator<A>, Function<Policy, T>> {
  default T apply(Configurator<A> configurator, Policy policy) {
    return this.apply(configurator).apply(policy);
  }

  static <A extends Attribute, T> Resolver<A, T> of(Function<Configurator<A>, Function<Policy, T>> func) {
    return of(func, () -> "Resolver(noname)");
  }

  static <A extends Attribute, T> Resolver<A, T> of(Function<Configurator<A>, Function<Policy, T>> func, Supplier<String> messageComposer) {
    return new Resolver<A, T>() {
      @Override
      public Function<Policy, T> apply(Configurator<A> configurator) {
        return policy -> func.apply(configurator).apply(policy);
      }

      @Override
      public String toString() {
        return messageComposer.get();
      }
    };
  }
}
