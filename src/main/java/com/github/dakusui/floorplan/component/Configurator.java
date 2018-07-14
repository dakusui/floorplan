package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.Utils;

import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface Configurator<A extends Attribute> extends AttributeBundle<A> {
  Configurator<A> configure(A attr, Resolver<A, ?> resolver);

  Configurator<A> addOperator(Operator<A> operator);

  Component<A> build(Policy policy, Map<Ref, Component<?>> pool);

  /**
   * Returns a resolver for a specified attribute {@code attr} set to this object
   * itself. If it is not present, an empty {@code Optional} will be returned.
   *
   * @param attr An attribute for which
   * @param <T>  Type of a value of an attribute {@code attr}.
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
   */
  default <T> Resolver<A, T> resolverFor(A attr, Policy policy) {
    return this.<T>resolverFor(attr).orElseGet(() -> policy.fallbackResolverFor(this.ref(), attr));
  }

  class Impl<A extends Attribute> implements Configurator<A> {
    private final ComponentSpec<A>                spec;
    private final Map<A, Resolver<A, ?>>          resolvers = new LinkedHashMap<>();
    private final Ref                             ref;
    private final Map<Operator.Type, Operator<A>> operators;

    Impl(ComponentSpec<A> spec, String id) {
      this.spec = spec;
      this.ref = Ref.ref(this.spec, id);
      this.operators = new HashMap<Operator.Type, Operator<A>>() {{
        spec.operatorFactories().entrySet().stream().map(new Function<Entry<Operator.Type, Operator.Factory<A>>, Entry<Operator.Type, Operator<A>>>() {
          @Override
          public Entry<Operator.Type, Operator<A>> apply(Entry<Operator.Type, Operator.Factory<A>> entry) {
            return new Entry<Operator.Type, Operator<A>>() {
              Operator<A> value = entry.getValue().apply(spec);

              @Override
              public Operator.Type getKey() {
                return entry.getKey();
              }

              @Override
              public Operator<A> getValue() {
                return value;
              }

              @Override
              public Operator<A> setValue(Operator<A> value) {
                return this.value = value;
              }
            };
          }
        }).forEach(entry -> put(entry.getKey(), entry.getValue()));
      }};
    }

    @Override
    public Ref ref() {
      return this.ref;
    }

    @Override
    public ComponentSpec<A> spec() {
      return this.spec;
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

    @Override
    public Configurator<A> addOperator(Operator<A> operator) {
      this.operators.put(requireNonNull(operator.type()), requireNonNull(operator));
      return this;
    }

    @Override
    public Component<A> build(Policy policy, Map<Ref, Component<?>> pool) {
      return new Component.Impl<>(this.ref, new LinkedHashMap<A, Object>() {{
        spec.attributes().forEach(
            (A attr) -> {
              Object u;
              put(attr,
                  require(
                      u = Utils.resolve(attr, Impl.this, policy),
                      attr::test,
                      Exceptions.typeMismatch(attr, u)
                  ));
            });
      }},
          operators,
          pool
      );
    }

    @Override
    public String toString() {
      return String.format("configurator(%s)", this.ref);
    }
  }
}
