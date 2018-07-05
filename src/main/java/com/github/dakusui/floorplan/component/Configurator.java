package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.*;

import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface Configurator<A extends Attribute> extends AttributeBundle<A> {
  Configurator<A> configure(A attr, Resolver<A, ?> resolver);

  Configurator<A> configure(Operation op, Operator<A> operator);

  Component<A> build(Policy policy);

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
    private final ComponentSpec<A>       spec;
    private final Map<A, Resolver<A, ?>> resolvers = new LinkedHashMap<>();
    Ref ref;
    private Map<Operation, Operator<A>> operators = new HashMap<>();

    Impl(ComponentSpec<A> spec, String id) {
      this.spec = spec;
      this.ref = Ref.ref(this.spec, id);
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
    public Configurator<A> configure(Operation op, Operator<A> operator) {
      this.operators.put(requireNonNull(op), requireNonNull(operator));
      return this;
    }

    @Override
    public Component<A> build(Policy policy) {
      return new Component.Impl<>(this.ref, new LinkedHashMap<A, Object>() {{
        Arrays.stream(spec.attributes()).forEach(
            (A attr) -> {
              Object v = resolverFor(attr, policy)
                  .apply(attr)
                  .apply(Impl.this)
                  .apply(policy);
              put(
                  attr,
                  require(
                      v,
                      attr::test,
                      Exceptions.typeMismatch(attr.valueType(), v)
                  ));
            });
      }}, operators);
    }

    @Override
    public String toString() {
      return String.format("configurator(%s)", this.ref);
    }
  }
}
