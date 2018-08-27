package com.github.dakusui.floorplan.utils;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FloorPlanConfigurator;
import com.github.dakusui.floorplan.core.FloorPlanDescriptor;
import com.github.dakusui.floorplan.core.FloorPlanGraph;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.function.Function;

/**
 * A utility class that collects useful methods for users of the 'FloorPlan' library.
 */
public enum FloorPlanUtils {
  ;
  @SuppressWarnings("unchecked")
  public static <A extends Attribute, T> T resolve(A attr, Configurator<A> configurator, Policy policy) {
    return (T) Function.class.cast(Function.class.cast(configurator.resolverFor(attr, policy).apply(configurator))).apply(policy);
  }

  public static FloorPlan buildFloorPlan(FloorPlanDescriptor floorPlanDescriptor) {
    return createFloorPlan(floorPlanDescriptor, createPolicy(floorPlanDescriptor));
  }

  @SuppressWarnings("unchecked")
  private static FloorPlan createFloorPlan(FloorPlanDescriptor floorPlanDescriptor, Policy policy) {
    FloorPlanConfigurator floorPlanConfigurator = policy.fixtureConfigurator();
    floorPlanDescriptor.attributes().forEach(
        each -> floorPlanConfigurator.configure(
            each.target,
            each.attribute,
            each.resolver
        )
    );
    return floorPlanConfigurator.build();
  }

  private static Policy createPolicy(FloorPlanDescriptor floorPlanDescriptor) {
    Policy.Builder policyBuilder = new Policy.Builder().setProfile(
        floorPlanDescriptor.profile()
    );
    floorPlanDescriptor.specs().forEach(policyBuilder::addComponentSpec);
    policyBuilder.setFloorPlanGraph(createFloorPlanGraph(floorPlanDescriptor));
    return policyBuilder.build();
  }

  private static FloorPlanGraph createFloorPlanGraph(FloorPlanDescriptor floorPlanDescriptor) {
    FloorPlanGraph floorPlanGraph = new FloorPlanGraph.Impl();
    floorPlanDescriptor.refs().forEach(floorPlanGraph::add);
    floorPlanDescriptor.wires().forEach(each -> floorPlanGraph.wire(each.from, each.as, each.tos));
    return floorPlanGraph;
  }
}
