package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;

import java.util.function.Function;

@FunctionalInterface
public interface ActionFactory<A extends Attribute> extends Function<Component<A>, Action> {
  default Action apply(Component<A> component) {
    return create(component);
  }

  Action create(Component<A> component);

  static <A extends Attribute> ActionFactory<A> of(Function<Component<A>, Action> func) {
    return func::apply;
  }
}
