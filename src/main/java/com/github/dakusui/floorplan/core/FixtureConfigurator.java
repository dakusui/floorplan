package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static com.github.dakusui.floorplan.utils.InternalUtils.singletonCollector;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface FixtureConfigurator {
  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  Set<Ref> allReferences();

  Fixture build();

  default <A extends Attribute> FixtureConfigurator configure(Ref ref, A attr, Resolver<A, ?> resolver) {
    this.<A>lookUp(ref).configure(attr, resolver);
    return this;
  }

  class Impl implements FixtureConfigurator {
    private final Set<Ref>              refs;
    private final List<Configurator<?>> configurators;
    private final Policy                policy;
    private final Fixture.Factory       fixtureFactory;

    Impl(Policy policy, Set<Ref> refs, Fixture.Factory fixtureFactory) {
      this.policy = requireNonNull(policy);
      this.refs = unmodifiableSet(requireNonNull(refs));
      this.configurators = unmodifiableList(
          refs.stream().map(
              // Not all components require slots.
              (Function<Ref, Configurator<?>>) ref -> ref.spec().configurator(ref.id())
          ).collect(toList())
      );
      this.fixtureFactory = requireNonNull(fixtureFactory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Attribute> Configurator<A> lookUp(Ref ref) {
      return (Configurator<A>) requireState(
          configurators.stream().filter(
              c -> Objects.equals(ref, c.ref())
          ).collect(
              singletonCollector()
          ).orElseThrow(
              Exceptions.noSuchElement("Configurator for '%s' was not found.", ref)
          ), ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }

    public Set<Ref> allReferences() {
      return refs;
    }

    @Override
    public Fixture build() {
      return this.fixtureFactory.create(this.policy, this);
    }
  }
}
