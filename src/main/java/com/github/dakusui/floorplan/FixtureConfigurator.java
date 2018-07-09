package com.github.dakusui.floorplan;

import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static com.github.dakusui.floorplan.utils.Utils.singletonCollector;
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

  @SuppressWarnings("unchecked")
  default <A extends Attribute> FixtureConfigurator addOperatorFactory(Ref ref, Operator.Factory<A> operator) {
    this.<A>lookUp(ref).addOperator(operator.apply((ComponentSpec<A>) ref.spec()));
    return this;
  }

  class Impl implements FixtureConfigurator {
    private final Set<Ref>              refs;
    private final List<Configurator<?>> configurators;
    private final Policy                policy;

    Impl(Policy policy, Set<Ref> refs) {
      this.policy = requireNonNull(policy);
      this.refs = unmodifiableSet(refs);
      this.configurators = unmodifiableList(
          refs.stream().map(
              // Not all components require slots.
              (Function<Ref, Configurator<?>>) ref -> ref.spec().configurator(ref.id())
          ).collect(toList())
      );
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
              NoSuchElementException::new
          ), ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }

    public Set<Ref> allReferences() {
      return refs;
    }

    @Override
    public Fixture build() {
      return new Fixture.Impl(this.policy, this);
    }
  }

}
