package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface Component<A extends Attribute> extends AttributeBundle<A> {
  Function<Context, Action> actionFactoryFor(Operation op);

  default Function<Context, Action> install() {
    return actionFactoryFor(Operation.INSTALL);
  }

  default Function<Context, Action> start() {
    return actionFactoryFor(Operation.START);
  }

  default Function<Context, Action> stop() {
    return actionFactoryFor(Operation.STOP);
  }

  default Function<Context, Action> nuke() {
    return actionFactoryFor(Operation.NUKE);
  }

  default Function<Context, Action> uninstall() {
    return actionFactoryFor(Operation.UNINSTALL);
  }

  <T> T valueOf(A attr);

  class Impl<A extends Attribute> implements Component<A> {
    private final Ref                         ref;
    @SuppressWarnings(
        "MismatchedQueryAndUpdateOfCollection"/* This field is updated in its static block on assignment*/
    )
    private final Map<A, Object>              values;
    private final Map<Operation, Operator<A>> operators;

    Impl(Ref ref, Map<A, Object> values, Map<Operation, Operator<A>> operators) {
      this.ref = ref;
      this.values = new HashMap<A, Object>() {{
        putAll(requireNonNull(values));
      }};
      this.operators = requireNonNull(operators);
    }

    @Override
    public Ref ref() {
      return ref;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ComponentSpec<A> spec() {
      return (ComponentSpec<A>) ref.spec();
    }

    @Override
    public Function<Context, Action> actionFactoryFor(Operation op) {
      return this.operators.computeIfAbsent(
          requireNonNull(op),
          o -> Operator.unsupported()
      ).apply(
          this
      );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T valueOf(A attr) {
      requireNonNull(attr);
      return (T) this.values.get(attr);
    }

    @Override
    public String toString() {
      return String.format("component(%s)", ref());
    }
  }
}
