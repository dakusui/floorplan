package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;
import com.github.dakusui.floorplan.utils.ObjectSynthesizer;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.github.dakusui.floorplan.exception.Exceptions.rethrow;
import static com.github.dakusui.floorplan.utils.Checks.require;

/**
 * A {@code configurator} is created from a 'spec' (a {@code ComponentSpec} instance),
 * which defines a specification of a certain component.
 * <p>
 * And through this interface, users can 'configure' components under test before
 * instantiating a {@code Component} by {@code build} method.
 * <p>
 * In other words, a {@code Configurator} is a builder of a component.
 *
 * @param <A> A type of attributes that characterize a component built by this object.
 * @see ComponentSpec
 */
public interface Configurator<A extends Attribute> extends AttributeBundle<A> {
  /**
   * Configures a specified attribute using a given resolver.
   *
   * @param attr     An attribute to be configured
   * @param resolver A resolver that provides a value to be set to {@code attr}.
   * @return This object
   */
  Configurator<A> configure(A attr, Resolver<A, ?> resolver);

  /**
   * Builds a component instance using resolvers set to attributes of this object.
   *
   * @param policy A policy object
   * @param pool   A pool that stores mappings from {@code ref} objects to {@code component} objects.
   * @param <C>    A type of component built by this configurator object.
   * @return A built component.
   */
  <C extends Component<A>> C build(Policy policy, Map<Ref, Component<?>> pool);

  /**
   * Returns a resolver for a specified attribute {@code attr} set to this object
   * itself. If it is not present, an empty {@code Optional} will be returned.
   *
   * @param attr An attribute for which a resolver is searched.
   * @param <T>  Type of a value of an attribute {@code attr}.
   * @return An optional of a resolver for the given attribute {@code attr}.
   */
  <T> Optional<Resolver<A, T>> resolverFor(A attr);

  /**
   * Returns a resolver for a specified attribute {@code attr}. If no resolver is
   * set to this object, this method returns tries to find a resolver from a
   * given {@code policy} object.
   *
   * @param attr   An attribute for which a resolver will be returned.
   * @param policy A policy object from which a resolver is searched.
   * @param <T>    A type of attribute value.
   * @return A resolver for the given attribute {@code attr}.
   */
  default <T> Resolver<A, T> resolverFor(A attr, Policy policy) {
    require(
        attr,
        (A a) -> a.spec().getClass().isAssignableFrom(this.spec().getClass()),
        n -> Exceptions.inconsistentSpec(
            () ->
                String.format("An attribute '%s' is not compatible with '%s'", n.name(), this.spec())
        ));
    return this.<T>resolverFor(attr).orElseGet(() -> policy.fallbackResolverFor(this.ref(), attr));
  }

  class Impl<A extends Attribute> implements Configurator<A> {
    private final ComponentSpec<A>       spec;
    private final Map<A, Resolver<A, ?>> resolvers = new LinkedHashMap<>();
    private final Ref                    ref;

    Impl(ComponentSpec<A> spec, String id) {
      this.spec = spec;
      this.ref = Ref.ref(this.spec, id);
    }

    @Override
    public Ref ref() {
      return this.ref;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<Resolver<A, T>> resolverFor(A attr) {
      return resolvers.containsKey(attr) ?
          Optional.of((Resolver<A, T>) resolvers.get(attr)) :
          Optional.empty();
    }

    @Override
    public Configurator<A> configure(A attr, Resolver<A, ?> resolver) {
      this.resolvers.put(
          attr,
          resolver
      );
      return this;
    }

    @SuppressWarnings({ "unchecked", "JavaReflectionMemberAccess" })
    @Override
    public <C extends Component<A>> C build(Policy policy, Map<Ref, Component<?>> pool) {
      LinkedHashMap<Attribute, Supplier<Object>> values = composeValues(policy);
      Component<A> ret;
      Class<Component<A>> componentType = this.spec.componentType();
      if (componentType.equals(Component.class))
        ret = new Component.Impl<>(this.ref, (Map<A, Supplier<Object>>) values, pool);
      else if (componentType.isInterface())
        ret = ObjectSynthesizer.builder(componentType)
            .fallbackTo(new Component.Impl<>(this.ref, values, pool))
            .build()
            .synthesize();
      else {
        try {
          ret = componentType.getConstructor(Ref.class, Map.class, Map.class).newInstance(this.ref, values, pool);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
          throw rethrow(e);
        }
      }
      pool.put(this.ref, ret);
      return (C) ret;
    }

    @Override
    public String toString() {
      return String.format("configurator(%s)", this.ref);
    }

    @SuppressWarnings("unchecked")
    LinkedHashMap<Attribute, Supplier<Object>> composeValues(Policy policy) {
      return new LinkedHashMap<Attribute, Supplier<Object>>() {{
        spec.attributes().stream().peek((
            (Attribute attr) -> put(attr,
                () -> {
                  Object u;
                  return require(
                      u = FloorPlanUtils.resolve((A) attr, Impl.this, policy),
                      attr::test,
                      Exceptions.typeMismatch(attr, u)
                  );
                }))).forEach(
            attribute -> {
              ////
              // If the attribute is non-optional, resolve it once, create
              // a supplier from the resolved value, and create a new supplier,
              // and put it back. Otherwise, the type check defined in the
              // previous stage is not performed until a user accesses the
              // attribute even if it is non-optional (required).
              if (!attribute.isOptional()) {
                Object value = get(attribute).get();
                put(
                    attribute,
                    () -> value
                );
              }
            }
        );
      }};
    }
  }
}
