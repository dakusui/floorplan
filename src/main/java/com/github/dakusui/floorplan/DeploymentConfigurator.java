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

public interface DeploymentConfigurator {
  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  Set<Ref> allReferences();

  Deployment build();

  default <A extends Attribute> DeploymentConfigurator configure(Ref ref, A attr, Resolver<A, ?> resolver) {
    this.<A>lookUp(ref).configure(attr, resolver);
    return this;
  }

  default <A extends Attribute> DeploymentConfigurator configure(Ref ref, Operation op, Operator<A> operator) {
    this.<A>lookUp(ref).configure(op, operator);
    return this;
  }

  class Impl implements DeploymentConfigurator {
    private final Set<Ref>              refs;
    private final List<Configurator<?>> configurators;
    private final Policy                policy;

    Impl(Policy policy, Set<Ref> refs, Map<Connector, Ref> wires) {
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
    public Deployment build() {
      return new Deployment.Impl(this.policy, this);
    }
  }

}
