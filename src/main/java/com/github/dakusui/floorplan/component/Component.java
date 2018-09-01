package com.github.dakusui.floorplan.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * An instance of this interface represents an actual component; "Component Instance".
 *
 * @param <A> Attribute that characterizes an instance of this interface.
 */
public interface Component<A extends Attribute> extends AttributeBundle<A> {
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
    private final Ref                      ref;
    @SuppressWarnings(
        "MismatchedQueryAndUpdateOfCollection"/* This field is updated in its static block on assignment*/
    )
    private final Map<A, Supplier<Object>> values;
    private final Map<Ref, Component<?>>   pool;

    public Impl(Ref ref, Map<A, Supplier<Object>> values, Map<Ref, Component<?>> pool) {
      this.ref = ref;
      this.values = new HashMap<A, Supplier<Object>>() {{
        putAll(requireNonNull(values));
      }};
      this.pool = pool;
    }

    @Override
    public Ref ref() {
      return ref;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T valueOf(A attr) {
      return lookUpIfReference(resolve(attr));
    }

    public Object resolve(A attr) {
      return this.values.get(requireNonNull(attr)).get();
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
      return obj instanceof List
          ? (T) List.class.cast(obj).stream().map(this::lookUpIfReference_).collect(toList())
          : lookUpIfReference_(obj);
    }

    @SuppressWarnings("unchecked")
    private <T> T lookUpIfReference_(Object obj) {
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
