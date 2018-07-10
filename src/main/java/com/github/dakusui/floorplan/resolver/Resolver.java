package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Resolver<A extends Attribute, T> extends Function<A, Function<Configurator<A>, Function<Policy<?>, T>>> {
  default T apply(A attr, Configurator<A> configurator, Policy policy) {
    return this.apply(attr).apply(configurator).apply(policy);
  }

  static <A extends Attribute, T> Resolver<A, T> of(Function<A, Function<Configurator<A>, Function<Policy<?>, T>>> func) {
    return of(func, () -> "Resolver(noname)");
  }

  static <A extends Attribute, T> Resolver<A, T> of(Function<A, Function<Configurator<A>, Function<Policy<?>, T>>> func, Supplier<String> messageComposer) {
    return new Resolver<A, T>() {
      @Override
      public Function<Configurator<A>, Function<Policy<?>, T>> apply(A a) {
        return aConfigurator -> policy -> func.apply(a).apply(aConfigurator).apply(policy);
      }

      @Override
      public String toString() {
        return messageComposer.get();
      }
    };
  }
}
