package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

/**
 * An instance of this interface represents an actual component; "Component Instance".
 *
 * @param <A> Attribute that characterizes an instance of this interface.
 */
public interface Component<A extends Attribute> extends AttributeBundle<A> {
  /**
   * Returns an action which performs an operation specified by {@code op}.
   *
   * @param op A type of operation.
   * @return an action that performs specified operation.
   */
  Action actionFor(Operator.Type op);

  /**
   * This method should return an {@code Action} to perform installation.
   *
   * @return an action that performs installation.
   */
  default Action install() {
    return actionFor(Operator.Type.INSTALL);
  }

  /**
   * This method should return an {@code Action} to start up a component
   * represented by an instance of this interface.
   *
   * @return an action that starts up this component instance.
   */
  default Action start() {
    return actionFor(Operator.Type.START);
  }

  /**
   * This method should return an {@code Action} to stop a component
   * represented by an instance of this interface.
   *
   * @return an action that stops this component instance.
   */
  default Action stop() {
    return actionFor(Operator.Type.STOP);
  }

  /**
   * This method should return an {@code Action} to kill a component
   * represented by an instance of this interface.
   *
   * @return an action that kills this component instance.
   */
  default Action nuke() {
    return actionFor(Operator.Type.NUKE);
  }


  /**
   * This method should return an {@code Action} to perform uninstallation.
   *
   * @return an action that performs uninstallation.
   */
  default Action uninstall() {
    return actionFor(Operator.Type.UNINSTALL);
  }

  /**
   * Returns a value of a specified attribute.
   *
   * @param attr an attribute whose value is returned.
   * @param <T>  Type of attribute value.
   * @return a value of the attribute {@code attr}.
   */
  <T> T valueOf(A attr);

  /**
   * For a list attribute {@code attr}, returns a specified element in the list.
   * If {@code attr} is not a list, the behaviour is not defined.
   *
   * @param attr  a list attribute whose element is returned.
   * @param index an index that specifies an element in the list.
   * @param <T>   a type of elements in the list
   * @return an element in the list specified by {@code index}.
   */
  <T> T valueOf(A attr, int index);

  /**
   * For a list attribute {@code attr}, returns the size of the list.
   * If {@code attr} is not a list, the behaviour is not defined.
   *
   * @param attr a list attribute whose size is returned.
   * @return the size of the list.
   */
  int sizeOf(A attr);

  /**
   * For a list attribute {@code attr}, streams its content.
   * If {@code attr} is not a list, the behaviour is not defined.
   *
   * @param attr a list attribute whose content is streamed
   * @param <T>  a type of elelments in the list.
   * @return a stream of the content of the list.
   */
  default <T> Stream<T> streamOf(A attr) {
    return IntStream.range(0, sizeOf(attr)).mapToObj(
        i -> valueOf(attr, i)
    );
  }

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
    public Action actionFor(Operator.Type op) {
      return this.operators.computeIfAbsent(
          requireNonNull(op),
          o -> (Operator<A>) Operator.Factory.unsupported(op).apply(spec())
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
    public int sizeOf(A attr) {
      return List.class.cast(this.<List>valueOf(attr)).size();
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
