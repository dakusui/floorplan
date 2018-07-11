package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.function.Predicate;

import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static java.util.Objects.requireNonNull;

/**
 * This interface is expected to be implemented by an {@code Enum} class that
 * is defined for a certain component and enumerates attributes belonging to
 * the component.
 */
public interface Attribute {
  default String name() {
    requireState(this, self -> self.getClass().isEnum());
    return ((Enum) this).getClass().getName();
  }

  @SuppressWarnings("unchecked")
  default <A extends Attribute> Resolver<A, ?> defaultValueResolver() {
    return (Resolver<A, ?>) bean().defaultValueResolver;
  }

  /**
   * Checks if the given {@code value} is an instance of a class returned by
   * {@code valueType()}.
   */
  default boolean test(Object value) {
    return bean().constraint.test(value);
  }

  /**
   * Returns a type of value to be held by this attribute.
   * Note that the type returned by this method is not always equal to the type of the value returned
   * by {@code Component#resolverFor} with this object if an arity of this attribute is an {@code ARRAY}, not an
   * {@code ATOM}.
   * In this case, a class object returned by this method represents a type of each element in the returned list
   * of the method {@code Component#resolverFor}.
   *
   * @return Type of this attribute.
   */
  default Class<?> valueType() {
    return bean().valueType;
  }

  /**
   * Returns a {@code ComponentSpec} object to which this attribute belongs.
   *
   * @param <A> Type of this attribute
   * @return A spec to which this attribute belongs.
   */
  @SuppressWarnings("unchecked")
  default <A extends Attribute> ComponentSpec<A> spec() {
    return (ComponentSpec<A>) bean().spec;
  }

  default String describeConstraint() {
    return bean().constraint.toString();
  }

  /**
   * Returns a bean class of this attribute interface.
   * Accessing this method from outside this interface is discouraged.
   *
   * @param <A> Type of the attribute
   * @param <B> Type of the bean
   */
  <A extends Attribute, B extends Bean<A>> B bean();

  final class Bean<A extends Attribute> {
    /**
     * Spec of the component to which this attribute belongs.
     */
    final ComponentSpec<A>  spec;
    /**
     * A function to resolve a default value of an attribute.
     */
    final Resolver<A, ?>    defaultValueResolver;
    /**
     * A constraint to be satisfied by a value for the attribute to which this
     * bean belongs.
     */
    final Predicate<Object> constraint;
    /**
     * Type of value the attribute holds.
     */
    final Class             valueType;

    private Bean(
        Class<?> valueType,
        ComponentSpec<A> spec,
        Resolver<A, ?> defaultValueResolver,
        Predicate<Object> constraint
    ) {
      requireNonNull(valueType);
      this.spec = requireNonNull(spec);
      this.defaultValueResolver = defaultValueResolver;
      this.constraint = constraint;
      this.valueType = valueType;
    }

    public static class Builder<A extends Attribute> {
      private final ComponentSpec<A>  spec;
      private       Class<?>          valueType;
      private       Resolver<A, ?>    defaultValueResolver = null;
      private       Predicate<Object> constraint           = null;

      /**
       * @param spec A spec of a component to which the attribute belongs
       */
      Builder(ComponentSpec<A> spec, Class<?> valueType, Predicate<Object> constraint) {
        this.spec = requireNonNull(spec);
        this.valueType = valueType;
        this.constraint = requireNonNull(constraint);
      }

      /**
       * Sets a resolver for a default value of the attribute.
       *
       * @param resolver A curried function to resolve the default value.
       * @return this object
       */
      @SuppressWarnings("unchecked")
      public Bean.Builder<A> defaultsTo(Resolver<A, ?> resolver) {
        this.defaultValueResolver = resolver;
        return this;
      }

      /**
       * Builds a {@code Bean} instance based on values given to this builder object.
       */
      public Bean<A> $() {
        return new Bean<>(
            this.valueType,
            this.spec,
            this.defaultValueResolver,
            this.constraint
        );
      }
    }
  }
}