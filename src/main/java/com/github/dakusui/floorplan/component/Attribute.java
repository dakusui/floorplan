package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.InternalUtils;
import com.github.dakusui.floorplan.utils.ObjectSynthesizer;
import com.github.dakusui.osynth.core.MethodHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.floorplan.resolver.Resolvers.nothing;
import static com.github.dakusui.floorplan.utils.Checks.requireArgument;
import static java.util.Objects.requireNonNull;

/**
 * This interface is expected to be implemented by an {@code Enum} class that
 * is defined for a certain component and enumerates attributes belonging to
 * the component.
 */
public interface Attribute {
  /**
   * Returns a name of this attribute. Implementation can be given usually by {@code Enum}'s
   * {@code name()} method. In case an {@code Attribute} is implemented by extending
   * existing one, in other words instances of it are created by {@code Attribute#create(...)}
   * methods, they will return values given to the methods as {@code name} argument.
   *
   * @return A name of this attribute.
   */
  default String name() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  default <A extends Attribute> Resolver<? super A, ?> defaultValueResolver() {
    return definition().defaultValueResolverFactory.apply(this);
  }

  /**
   * Checks if a given value can become a value of this attribute. Typically,
   * if the {@code value} is an instance of a class returned through {@code valueType()},
   * {@code true} will be returned. This can be said the definition of this object is
   * created by {@code ComponentSpec#property(Class&lt;?&gt; type)} method.
   * <p>
   * For more detail, refer to methods in {@code ComponentSpec} class that return
   * {@code Attribute.Bean.Builder} object.
   *
   * @param value A value to be tested.
   * @return {@code true} - {@code value} can become a value of this attribute/ {@code false} - otherwise
   * @see ComponentSpec
   */
  default boolean test(Object value) {
    return definition().constraint.test(value);
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
    return definition().valueType;
  }

  /**
   * Returns a {@code ComponentSpec} object to which this attribute belongs.
   *
   * @param <A> Type of this attribute
   * @return A spec to which this attribute belongs.
   */
  @SuppressWarnings("unchecked")
  default <A extends Attribute> ComponentSpec<A> spec() {
    return (ComponentSpec<A>) definition().spec;
  }

  /**
   * Returns a description of a constraint held by this object.
   *
   * @return A description of a constraint.
   */
  default String describeConstraint() {
    return definition().constraint.toString();
  }

  /**
   * Returns more specialized one from {@code this} object and given {@code another}
   * object.
   * <p>
   * Being "more specialized" means defined in a subclass.
   * <p>
   * If {@code this} and {@code another} have different names, an exception will
   * be thrown.
   *
   * @param another Another object to be compared with this object.
   * @param <A>     Type of this attribute.
   * @return More specialized one from this and another.
   */
  @SuppressWarnings("unchecked")
  default <A extends Attribute> Optional<A> moreSpecialized(A another) {
    requireNonNull(another);
    requireArgument(this.name(), n -> Objects.equals(n, another.name()));
    if (this.spec().attributeType().isAssignableFrom(another.spec().attributeType()))
      return Optional.of(another);
    if (another.spec().attributeType().isAssignableFrom(this.spec().attributeType()))
      return Optional.of((A) this);
    return Optional.empty();
  }

  default boolean isOptional() {
    return definition().isOptional;
  }

  /**
   * Returns a definition object of this attribute interface.
   * Accessing this method from outside this interface is discouraged.
   *
   * @param <A> Type of the attribute
   * @param <B> Type of the definition
   * @return A definition object of this attribute.
   */
  <A extends Attribute, B extends Definition<A>> B definition();

  /**
   * @param definition An object that holds contents of the attribute to be created.
   * @param <A>        A type of attribute, represented by {@code attrType}.
   * @return Created attribute.
   */
  static <A extends Attribute> A create(Definition<A> definition) {
    class Attr implements Attribute {
      private String attrName = null;

      @SuppressWarnings("unchecked")
      @Override
      public <AA extends Attribute, B extends Definition<AA>> B definition() {
        return (B) definition;
      }

      public synchronized String toString() {
        /*
        return attrName == null
            ? String.format("%s.(noname)@%d", spec().attributeType().getSimpleName(), System.identityHashCode(this))
            : attrName;

         */
        return name();
      }

      public synchronized String name() {
        if (attrName == null)
          attrName = InternalUtils.determineAttributeName(definition.spec.attributeType(), this);
        return attrName;
      }
    }
    Attr fallback = new Attr();
    return ObjectSynthesizer.builder(definition.spec.attributeType())
        .handle(new ObjectSynthesizer.Handler.Builder(
            method -> method.getName().equals("name") && method.getParameterCount() == 0)
            .with((o, args) -> fallback.name()))
        .handle(MethodHandler.toStringHandler(fallback, v -> ((Attr) v).name()))
        .fallbackTo(fallback)
        .build()
        .synthesize();
  }

  final class Definition<A extends Attribute> {
    /**
     * Spec of the component to which this attribute belongs.
     */
    final ComponentSpec<A>            spec;
    /**
     * A function to resolve a default value of an attribute.
     */
    final Function<A, Resolver<A, ?>> defaultValueResolverFactory;
    /**
     * A constraint to be satisfied by a value for the attribute to which this
     * definition belongs.
     */
    final Predicate<Object>           constraint;
    /**
     * Type of value the attribute holds.
     */
    final Class                       valueType;
    final boolean                     isOptional;

    private Definition(
        Class<?> valueType,
        ComponentSpec<A> spec,
        Function<A, Resolver<A, ?>> defaultValueResolverFactory,
        Predicate<Object> constraint,
        boolean isOptional) {
      requireNonNull(valueType);
      this.spec = requireNonNull(spec);
      this.defaultValueResolverFactory = defaultValueResolverFactory;
      this.constraint = constraint;
      this.valueType = valueType;
      this.isOptional = isOptional;
    }

    public static class Builder<A extends Attribute> {
      private final ComponentSpec<A>            spec;
      private       Class<?>                    valueType;
      private       Function<A, Resolver<A, ?>> defaultValueResolverFactory = null;
      private       Predicate<Object>           constraint;
      private       boolean                     isOptional                  = false;

      /**
       * @param spec A spec of a component to which the attribute belongs
       */
      Builder(ComponentSpec<A> spec, Class<?> valueType, Predicate<Object> constraint) {
        this.spec = requireNonNull(spec);
        this.valueType = valueType;
        this.constraint = requireNonNull(constraint);
        this.required();
      }

      /**
       * Sets a resolver for a default value of the attribute.
       *
       * @param resolver A curried function to resolve the default value.
       * @return this object
       */
      @SuppressWarnings("unchecked")
      public Definition.Builder<A> defaultsTo(Resolver<? super A, ?> resolver) {
        return this.defaultsTo_(a -> (Resolver<A, ?>) resolver);
      }

      /**
       * Sets the attribute definition represented by this object "required".
       * A value of an attribute marked required is validated when a component
       * to which it belongs is built by a configurator.
       *
       * @return This object
       */
      @SuppressWarnings("unchecked")
      public Definition.Builder<A> required() {
        this.isOptional = false;
        return this.defaultsTo_(Function.class.cast(nothing()));
      }

      /**
       * Sets the attribute definition represented by this object "optional".
       * A value of an attribute marked optional is validated when it is retrieved
       * from a component instance.
       *
       * @return This object.
       */
      public Definition.Builder<A> optional() {
        this.isOptional = true;
        return this.defaultsTo_(nothing());
      }

      @SuppressWarnings("unchecked")
      private Definition.Builder<A> defaultsTo_(Function<A, Resolver<A, ?>> resolverFactory) {
        this.defaultValueResolverFactory = Function.class.cast(resolverFactory);
        return this;
      }

      /**
       * Builds a {@code Bean} instance based on values given to this builder object.
       *
       * @return A new definition object.
       */
      public Definition<A> $() {
        return new Definition<>(
            this.valueType,
            this.spec,
            this.defaultValueResolverFactory,
            this.constraint,
            this.isOptional);
      }

      /**
       * Creates a new attribute object from the attribute definition built by this
       * object.
       *
       * @return A created attribute object.
       */
      public A define() {
        return Attribute.create(this.$());
      }
    }
  }
}