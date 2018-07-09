package com.github.dakusui.floorplan;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;

import java.util.*;

import static com.github.dakusui.floorplan.Connector.connector;
import static com.github.dakusui.floorplan.component.Ref.ref;
import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static java.util.stream.Collectors.toList;

public class FloorPlan {

  private final Set<Ref>            refs  = new LinkedHashSet<>();
  private final Map<Connector, Ref> wires = new LinkedHashMap<>();

  public FloorPlan() {
  }

  /**
   * If you want to install 2 gateway clusters, you will call this method twice with an instance of
   * a gateway's spec.
   *
   * @param spec A spec instance
   * @param name A name for a configurator which is created from {@code spec}.
   * @return this object.
   */
  @SuppressWarnings("unchecked")
  public <A extends Attribute> FloorPlan add(ComponentSpec<A> spec, String name) {
    return this.add(ref(spec, name));
  }

  @SuppressWarnings("unchecked")
  public FloorPlan add(Ref... refs) {
    this.refs.addAll(Arrays.asList(refs));
    return this;
  }

  public FloorPlan wire(
      Ref from,
      Ref to,
      Attribute as
  ) {
    requireState(
        from, r -> this.refs.contains(from),
        a -> String.format("'%s' is not yet added to this object. (from)", a)
    );
    requireState(
        to, r -> this.refs.contains(from),
        a -> String.format("'%s' is not yet added to this object. (to)", a)
    );
    this.wires.put(
        connector(from, as),
        to
    );
    return this;
  }

  public Set<Ref> allReferences() {
    return Collections.unmodifiableSet(this.refs);
  }

  public FixtureConfigurator configurator(Policy policy) {
    return new FixtureConfigurator.Impl(policy, refs, wires);
  }

  public List<? extends ResolverEntry> allWires() {
    return this.wires.entrySet().stream().map(entry -> new ResolverEntry(
        (ref, attribute) -> entry.getKey().equals(Connector.connector(ref, attribute)),
        Resolver.of(a -> c -> p -> entry.getValue())
    )).collect(toList()
    );
  }
}
