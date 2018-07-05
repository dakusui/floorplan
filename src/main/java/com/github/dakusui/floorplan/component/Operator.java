package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.exception.Exceptions;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Operator<A extends Attribute> extends Function<Component<A>, Function<Context, Action>> {

  static <A extends Attribute> Operator<A> of(Function<Component<A>, Function<Context, Action>> func, Supplier<String> messageComposer) {
    return new Operator<A>() {
      @Override
      public Function<Context, Action> apply(Component<A> component) {
        return context -> func.apply(component).apply(context);
      }

      @Override
      public String toString() {
        return messageComposer.get();
      }
    };
  }

  static <A extends Attribute> Operator<A> nop() {
    return Operator.of(
        component -> Context::nop,
        () -> "nop"
    );
  }

  static <A extends Attribute> Operator<A> unsupported() {
    return Operator.of(
        component -> context -> {
          throw Exceptions.throwUnsupportedOperation(String.format("This operation is not supported by '%s'", component));
        },
        () -> "unsupported"
    );
  }
}
