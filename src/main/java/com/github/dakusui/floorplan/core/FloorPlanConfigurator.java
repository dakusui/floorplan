package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.*;
import java.util.function.Consumer;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static com.github.dakusui.floorplan.utils.InternalUtils.singletonCollector;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public interface FloorPlanConfigurator {
  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  Set<Ref> allReferences();

  FloorPlan build();

  default <A extends Attribute> FloorPlanConfigurator configure(Ref ref, A attr, Resolver<A, ?> resolver) {
    this.<A>lookUp(ref).configure(attr, resolver);
    return this;
  }

  class Impl implements FloorPlanConfigurator {
    private final Set<Ref>                        refs;
    private final Map<Ref, List<Configurator<?>>> configurators;
    private final Policy                          policy;
    private final FloorPlan.Factory               floorPlanFactory;

    Impl(Policy policy, Set<Ref> refs, FloorPlan.Factory floorPlanFactory) {
      this.policy = requireNonNull(policy);
      this.refs = unmodifiableSet(requireNonNull(refs));

      this.configurators = unmodifiableMap(
          new HashMap<Ref, List<Configurator<?>>>() {
            {
              refs.forEach(
                  ref -> {
                    // Not all components require slots.
                    Configurator<?> c = ref.spec().configurator(ref.id());
                    Ref keyRef = c.ref();
                    if (!containsKey(keyRef))
                      put(keyRef, new LinkedList<>());
                    List<Configurator<?>> list = get(keyRef);
                    list.add(c);
                  }
              );
            }
          }
      );
      this.floorPlanFactory = requireNonNull(floorPlanFactory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Attribute> Configurator<A> lookUp(Ref ref) {
      if (!configurators.containsKey(ref))
        throw noSuchElement("Configurator for '%s' was not found.", ref).get();
      return (Configurator<A>) requireState(
          configurators
              .get(ref)
              .stream()
              .filter(c -> Objects.equals(ref, c.ref()))
              .collect(singletonCollector())
              .orElseThrow(noSuchElement("Configurator for '%s' was not found.", ref)),
          ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }

    public Set<Ref> allReferences() {
      return refs;
    }

    @Override
    public FloorPlan build() {
      return this.floorPlanFactory.create(this.policy, this);
    }
  }
}
