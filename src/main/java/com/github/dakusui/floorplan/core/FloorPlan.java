package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.dakusui.floorplan.utils.Checks.requireState;

public interface FloorPlan {
  <A extends Attribute, C extends Component<A>> C lookUp(Ref ref);

  interface Factory {
    FloorPlan create(Policy policy, FloorPlanConfigurator floorPlanConfigurator);
  }

  final class Impl implements FloorPlan {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Ref, Component<?>> components;

    public Impl(Policy policy, FloorPlanConfigurator floorPlanConfigurator) {
      this.components = new LinkedHashMap<Ref, Component<?>>() {{
        floorPlanConfigurator.allReferences().stream().map(
            ref -> (Configurator<?>) floorPlanConfigurator.lookUp(ref)
        ).filter(
            configurator -> !this.containsKey(configurator.ref())
        ).forEach(
            configurator -> configurator.build(policy, this)
        );
      }};
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Attribute, C extends Component<A>> C lookUp(Ref ref) {
      return (C) requireState(
          (Component<A>) this.components.get(ref),
          ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }
  }
}
