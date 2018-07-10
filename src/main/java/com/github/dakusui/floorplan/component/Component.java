package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.exception.Exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface Component<A extends Attribute> extends AttributeBundle<A> {
  interface ActionFactory extends Function<Context, Action> {
  }
  ActionFactory actionFactoryFor(Operator.Type op);

  /**
   * This method should return
   */
  default ActionFactory install() {
    return actionFactoryFor(Operator.Type.INSTALL);
  }

  default ActionFactory start() {
    return actionFactoryFor(Operator.Type.START);
  }

  default ActionFactory stop() {
    return actionFactoryFor(Operator.Type.STOP);
  }

  default ActionFactory nuke() {
    return actionFactoryFor(Operator.Type.NUKE);
  }

  default ActionFactory uninstall() {
    return actionFactoryFor(Operator.Type.UNINSTALL);
  }

  <T> T valueOf(A attr);

  <T> T valueOf(A attr, int index);

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
    public ActionFactory actionFactoryFor(Operator.Type op) {
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
      return lookUpIfReference(this.values.get(requireNonNull(attr)));
    }

    @Override
    public <T> T valueOf(A attr, int index) {
      return lookUpIfReference(List.class.cast(this.<List<T>>valueOf(attr)).get(index));
    }

    @Override
    public String toString() {
      return String.format("component(%s)", ref());
    }

    @SuppressWarnings("unchecked")
    private <T> T lookUpIfReference(Object obj) {
      return (T) (obj instanceof Ref ?
          require(
              this.pool.get(obj),
              Objects::nonNull,
              noSuchElement("Component '%s' was not found", obj)
          ) :
          obj);
    }
  }
}
