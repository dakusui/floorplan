package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.dakusui.floorplan.exception.Exceptions.typeMismatch;
import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum Resolvers {
  ;

  public static <A extends Attribute, T> Resolver<A, T> immediate(T value) {
    return Resolver.of(
        a -> c -> p -> value,
        () -> String.format("immediate(%s)", value)
    );
  }

  public static <A extends Attribute, B extends Attribute> Resolver<A, Configurator<B>> referenceTo(Ref ref) {
    return Resolver.of(
        a -> c -> p -> p.fixtureConfigurator().lookUp(ref),
        () -> String.format("referenceTo(component:%s)", ref)
    );
  }

  public static <A extends Attribute, T> Resolver<A, T> referenceTo(A another) {
    requireNonNull(another);
    return Resolver.of(
        a -> c -> p -> Utils.resolve(another, c, p),
        () -> String.format("referenceTo(attr:%s)", another)
    );
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute, B extends Attribute, T extends Configurator<B>, R> Resolver<A, R> attributeValueOf(B attr, Resolver<A, T> holder) {
    return Resolver.of(
        a -> c -> p -> Utils.resolve(attr, holder.apply(a).apply(c).apply(p), p),
        () -> String.format("attributeValueOf(%s, %s)", attr, holder)
    );
  }

  public static <A extends Attribute, T> Resolver<A, T> profileValue(String key) {
    return Resolver.of(
        a -> c -> p -> p.profile().<A, T>resolverFor(key).apply(a).apply(c).apply(p),
        () -> String.format("profileValueOf(%s)", key)
    );
  }

  /**
   * Returned resolver will give a value specified by a {@code key} from a slot.
   *
   * @param key A key to specify a value in a slot
   * @param <A> Type of attribute
   * @param <T> Type of returned value.
   */
  public static <A extends Attribute, T> Resolver<A, T> slotValue(String key) {
    return Resolver.of(
        a -> c -> p -> p.profile().slotFor(c.ref()).<A, T>resolverFor(key).apply(a).apply(c).apply(p),
        () -> String.format("slotValueOf(%s)", key)
    );
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute, E>
  Resolver<A, List<E>> listOf(Class<E> type, Resolver<A, ? extends E>... resolvers) {
    return Resolver.of(
        a -> c -> p ->
            Arrays.stream(resolvers).map(
                resolver -> resolver.apply(a, c, p)
            ).map(
                e -> require(e, o -> o == null || type.isAssignableFrom(o.getClass()), typeMismatch(type, e))
            ).collect(Collectors.toList())
        ,
        () -> String.format(
            "listOf(%s)",
            String.join(
                ",",
                Arrays.stream(resolvers).map(Object::toString).collect(toList())))
    );
  }

  /**
   * Returns a resolver that always throw a missing value exception.
   * This method is useful to define an attribute that does not have a default value,
   * i.e., a 'required' attribute.
   *
   * @param <A> Type of attribute
   * @param <T> Type of an expected value.
   * @return A resolver that throws an exception always when applied.
   */
  public static <A extends Attribute, T> Resolver<A, T> nothing() {
    return Resolver.of(
        a -> c -> p -> {
          throw Exceptions.missingValue(c.ref(), a).get();
        },
        () -> "nothing"
    );
  }

}
