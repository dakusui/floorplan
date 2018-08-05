package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * A {@code onfigurator} is created from a 'spec' (a {@code ComponentSpec} instance),
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
   * Adds a specified operator factory to this object. The operator will be added
   * to a component built by this object.
   *
   * @param operator An operator to be added.
   * @return This object
   */
  Configurator<A> addOperatorFactory(Operator.Factory<A> operator);

  /**
   * Builds a component instance using resolvers set to attributes of this object.
   *
   * @param policy A policy object
   * @param pool   A pool that stores mappings from {@code ref} objects to {@code component} objects.
   * @return A built component.
   */
  Component<A> build(Policy policy, Map<Ref, Component<?>> pool);

  /**
   * Returns a resolver for a specified attribute {@code attr} set to this object
   * itself. If it is not present, an empty {@code Optional} will be returned.
   *
   * @param attr An attribute for which a resolver is searched.
   * @param <T>  Type of a value of an attribute {@code attr}.
   * @return An optional of a resolver for the given attribute {@code attr}.
   */
  <T> Optional<Resolver<? super A, T>> resolverFor(A attr);

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
  default <T> Resolver<? super A, T> resolverFor(A attr, Policy policy) {
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
    private final ComponentSpec<A>                        spec;
    private final Map<A, Resolver<A, ?>>                  resolvers = new LinkedHashMap<>();
    private final Ref                                     ref;
    private final Map<Operator.Type, Operator.Factory<A>> operatorFactories;

    Impl(ComponentSpec<A> spec, String id) {
      this.spec = spec;
      this.ref = Ref.ref(this.spec, id);
      this.operatorFactories = this.spec.operatorFactories();

    }

    @Override
    public Ref ref() {
      return this.ref;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<Resolver<? super A, T>> resolverFor(A attr) {
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

    @SuppressWarnings("unchecked")
    @Override
    public Configurator<A> addOperatorFactory(Operator.Factory<A> operatorFactory) {
      this.operatorFactories.put(requireNonNull(operatorFactory.type()), requireNonNull(operatorFactory));
      return this;
    }

    @Override
    public Component<A> build(Policy policy, Map<Ref, Component<?>> pool) {
      return new Component.Impl<>(
          this.ref,
          new LinkedHashMap<A, Object>() {{
            spec.attributes().forEach(
                (A attr) -> {
                  Object u;
                  put(attr,
                      require(
                          u = FloorPlanUtils.resolve(attr, Impl.this, policy),
                          attr::test,
                          Exceptions.typeMismatch(attr, u)
                      ));
                });
          }},
          operatorFactories.entrySet().stream().map(this::convertEntry
          ).collect(toMap(Map.Entry::getKey, Map.Entry::getValue)),
          pool
      );
    }

    Map.Entry<Operator.Type, Operator<A>> convertEntry(Map.Entry<Operator.Type, Operator.Factory<A>> inEntry) {
      return new Map.Entry<Operator.Type, Operator<A>>() {
        Operator<A> value = inEntry.getValue().apply(spec());

        @Override
        public Operator.Type getKey() {
          return inEntry.getKey();
        }

        @Override
        public Operator<A> getValue() {
          return value;
        }

        @Override
        public Operator<A> setValue(Operator<A> value) {
          this.value = value;
          return value;
        }
      };
    }


    @Override
    public String toString() {
      return String.format("configurator(%s)", this.ref);
    }
  }
}
