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

public interface Fixture {
  <A extends Attribute> Component<A> lookUp(Ref ref);

  interface Factory {
    Fixture create(Policy policy, FixtureConfigurator fixtureConfigurator);
  }

  abstract class Base implements Fixture {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Ref, Component<?>> components;

    protected Base(Policy policy, FixtureConfigurator fixtureConfigurator) {
      this.components = new LinkedHashMap<Ref, Component<?>>() {{
        fixtureConfigurator.allReferences().stream().map(
            ref -> (Configurator<?>) fixtureConfigurator.lookUp(ref)
        ).filter(
            configurator -> !this.containsKey(configurator.ref())
        ).forEach(
            configurator -> configurator.build(policy, this)
        );
      }};
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Attribute> Component<A> lookUp(Ref ref) {
      return requireState(
          (Component<A>) this.components.get(ref), ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }
  }
}
