package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface Component<A extends Attribute> extends AttributeBundle<A> {
  Function<Context, Action> actionFactoryFor(Operator.Type op);

  default Function<Context, Action> install() {
    return actionFactoryFor(Operator.Type.INSTALL);
  }

  default Function<Context, Action> start() {
    return actionFactoryFor(Operator.Type.START);
  }

  default Function<Context, Action> stop() {
    return actionFactoryFor(Operator.Type.STOP);
  }

  default Function<Context, Action> nuke() {
    return actionFactoryFor(Operator.Type.NUKE);
  }

  default Function<Context, Action> uninstall() {
    return actionFactoryFor(Operator.Type.UNINSTALL);
  }

  <T> T valueOf(A attr);

  class Impl<A extends Attribute> implements Component<A> {
    private final Ref                             ref;
    @SuppressWarnings(
        "MismatchedQueryAndUpdateOfCollection"/* This field is updated in its static block on assignment*/
    )
    private final Map<A, Object>                  values;
    private final Map<Operator.Type, Operator<A>> operators;
    private final Map<Ref, Component<?>>          pool;

    Impl(Ref ref, Map<A, Object> values, Map<Operator.Type, Operator<A>> operators, Map<Ref, Component<?>> pool) {
      this.ref = ref;
      this.values = new HashMap<A, Object>() {{
        putAll(requireNonNull(values));
      }};
      this.operators = requireNonNull(operators);
      this.pool = pool;
      this.pool.put(this.ref, this);
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

    @SuppressWarnings("unchecked")
    @Override
    public Function<Context, Action> actionFactoryFor(Operator.Type op) {
      return this.operators.computeIfAbsent(
          requireNonNull(op),
          o -> (Operator<A>) Operator.Factory.unsupported(op).apply((ComponentSpec<Attribute>) spec())
      ).apply(
          this
      );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T valueOf(A attr) {
      Object ret = this.values.get(requireNonNull(attr));
      return (T) ((ret instanceof Ref) ?
          pool.get(ret) :
          ret);
    }

    @Override
    public String toString() {
      return String.format("component(%s)", ref());
    }
  }
}
