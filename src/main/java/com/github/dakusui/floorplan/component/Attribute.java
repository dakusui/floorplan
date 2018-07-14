package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.ObjectSynthesizer;
import com.github.dakusui.floorplan.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dakusui.floorplan.exception.Exceptions.inconsistentSpec;
import static com.github.dakusui.floorplan.utils.Checks.require;
import static java.util.Objects.requireNonNull;

/**
 * This interface is expected to be implemented by an {@code Enum} class that
 * is defined for a certain component and enumerates attributes belonging to
 * the component.
 */
public interface Attribute {
  default String name() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  default <A extends Attribute> Resolver<? super A, ?> defaultValueResolver() {
    return bean().defaultValueResolver;
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

  default <A extends Attribute> Optional<A> moreSpecialized(A another) {
    requireNonNull(another);
    if (this.spec().attributeType().isAssignableFrom(another.spec().attributeType()))
      return Optional.of(another);
    if (another.spec().attributeType().isAssignableFrom(this.spec().attributeType()))
      return Optional.of((A) this);
    return Optional.empty();
  }

  /**
   * Returns a bean class of this attribute interface.
   * Accessing this method from outside this interface is discouraged.
   *
   * @param <A> Type of the attribute
   * @param <B> Type of the bean
   */
  <A extends Attribute, B extends Bean<A>> B bean();

  static <A extends Attribute> A create(String attrName, Bean<A> bean) {
    return create(attrName, bean.spec.attributeType(), bean);
  }

  /**
   * Use this method with care.
   * This method is used to create an attribute that needs to specify a type to which
   * it belongs. Such as an attribute defined in an interface, not in an {@code Enum},
   * and references to another defined in a super-interface.
   *
   * @param attrName
   * @param attrType
   * @param bean
   * @param <A>
   * @return
   */
  static <A extends Attribute> A create(String attrName, Class<A> attrType, Bean<?> bean) {
    return ObjectSynthesizer.builder(attrType)
        .fallbackTo(new Attribute() {
          @SuppressWarnings("unchecked")
          @Override
          public <AA extends Attribute, B extends Bean<AA>> B bean() {
            return (B) bean;
          }

          public String toString() {
            return attrName;
          }

          public String name() {
            return attrName;
          }
        })
        .synthesize();
  }

  @SuppressWarnings("unchecked")
  static <A extends Attribute> List<A> attributes(Class<A> attrType) {
    return new LinkedList<A>() {{
      addAll(Arrays.stream(attrType.getFields())
          .filter(field -> Modifier.isStatic(field.getModifiers()))
          .filter(field -> Modifier.isFinal(field.getModifiers()))
          .filter(field -> Attribute.class.isAssignableFrom(attrType))
          .filter(field -> field.getType().isAssignableFrom(field.getType()))
          .sorted(Comparator.comparing(Field::getName))
          .peek(field ->
              require(
                  Utils.<Attribute>getStaticFieldValue(field).name(),
                  n -> Objects.equals(n, field.getName()),
                  n -> inconsistentSpec(
                      () -> String.format(
                          "Attribute '%s' has to have the same name as the name of the field (%s) to which it is assigned.", n, field.getName()
                      ))))
          .map(Utils::getStaticFieldValue)
          .map(a -> (A) a)
          .collect(Collector.of(
              LinkedHashMap::new,
              (map, attr) -> {
                if (map.containsKey(attr.name())) {
                  map.put(
                      attr.name(),
                      attr.moreSpecialized(map.get(attr.name())).orElseThrow(
                          inconsistentSpec(inconsistentSpecMessageSupplier(map.get(attr.name()), attr)))
                  );
                } else
                  map.put(attr.name(), attr);
              },
              (Map<String, A> mapA, Map<String, A> mapB) -> new LinkedHashMap<String, A>() {{
                putAll(mapA);
                mapB.forEach((key, value) -> {
                  if (!containsKey(key))
                    put(key,
                        value.moreSpecialized(get(key))
                            .orElseThrow(inconsistentSpec(
                                inconsistentSpecMessageSupplier(get(key), value)))
                    );
                });
              }})).values());
    }};
  }

  static <A extends Attribute> Supplier<String> inconsistentSpecMessageSupplier(A attr1, A attr2) {
    return () -> String.format(
        "It cannot be determined which is more special between '%s'(%s) and '%s'(%s)",
        attr2,
        attr2.getClass(),
        attr1,
        attr1.getClass()
    );
  }

  final class Bean<A extends Attribute> {
    /**
     * Spec of the component to which this attribute belongs.
     */
    final ComponentSpec<A>       spec;
    /**
     * A function to resolve a default value of an attribute.
     */
    final Resolver<? super A, ?> defaultValueResolver;
    /**
     * A constraint to be satisfied by a value for the attribute to which this
     * bean belongs.
     */
    final Predicate<Object>      constraint;
    /**
     * Type of value the attribute holds.
     */
    final Class                  valueType;

    private Bean(
        Class<?> valueType,
        ComponentSpec<A> spec,
        Resolver<? super A, ?> defaultValueResolver,
        Predicate<Object> constraint
    ) {
      requireNonNull(valueType);
      this.spec = requireNonNull(spec);
      this.defaultValueResolver = defaultValueResolver;
      this.constraint = constraint;
      this.valueType = valueType;
    }

    public static class Builder<A extends Attribute> {
      private final ComponentSpec<A>       spec;
      private       Class<?>               valueType;
      private       Resolver<? super A, ?> defaultValueResolver = null;
      private       Predicate<Object>      constraint           = null;

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
      public Bean.Builder<A> defaultsTo(Resolver<? super A, ?> resolver) {
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