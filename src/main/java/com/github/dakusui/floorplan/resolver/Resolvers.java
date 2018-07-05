package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

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
        a -> c -> p -> p.deploymentConfigurator().lookUp(ref),
        () -> String.format("referenceTo(component:%s)", ref)
    );
  }

  public static <A extends Attribute, T> Resolver<A, T> referenceTo(A another) {
    requireNonNull(another);
    return Resolver.of(
        a -> c -> p -> c.<T>resolverFor(another, p).apply(another).apply(c).apply(p),
        () -> String.format("referenceTo(attr:%s)", another)
    );
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute, B extends Attribute, T extends Configurator<B>, R> Resolver<A, R> attributeValueOf(B attr, Resolver<A, T> holder) {
    return Resolver.of(
        a -> c -> p -> {
          T configurator = holder.apply(a).apply(c).apply(p);
          return (R) configurator.resolverFor(attr, p).apply(attr).apply(configurator).apply(p);
        },
        () -> String.format("attributeValueOf(%s, %s)", attr, holder)
    );
  }

  public static <A extends Attribute, T> Resolver<A, T> profileValue(String key) {
    return Resolver.of(
        a -> c -> p -> p.profile().<A, T>resolverFor(key).apply(a).apply(c).apply(p),
        () -> String.format("profileValueOf(%s)", key)
    );
  }

  public static <A extends Attribute, T> Resolver<A, T> slotValue(String key) {
    return Resolver.<A, T>of(
        a -> c -> p -> p.profile().slotFor(c.ref()).<A, T>resolverFor(key).apply(a).apply(c).apply(p),
        () -> String.format("slotValueOf(%s)", key)
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
