package com.github.dakusui.floorplan.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.Arrays;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.parallel;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static java.util.stream.Collectors.toList;

/**
 * A utility class that collects useful methods for users of the 'FloorPlan' library.
 */
public enum FloorPlanUtils {
  ;

  public static Action createGroupedAction(
      boolean parallel,
      Function<Component<?>, Action> actionFactoryCreator,
      Fixture fixture,
      Ref... refs
  ) {
    Action[] actions = Arrays.stream(
        refs
    ).map(
        fixture::lookUp
    ).map(
        actionFactoryCreator::apply
    ).collect(
        toList()
    ).toArray(
        new Action[refs.length]
    );
    return parallel ?
        parallel(actions) :
        sequential(actions);
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute, T> T resolve(A attr, Configurator<A> configurator, Policy policy) {
    return (T) Function.class.cast(Function.class.cast(configurator.resolverFor(attr, policy).<A>apply(attr)).apply(configurator)).apply(policy);
  }

  public static Fixture buildFixture(FixtureDescriptor fixtureDescriptor) {
    return createFixture(fixtureDescriptor, createPolicy(fixtureDescriptor));
  }

  @SuppressWarnings("unchecked")
  private static Fixture createFixture(FixtureDescriptor fixtureDescriptor, Policy policy) {
    FixtureConfigurator fixtureConfigurator = policy.fixtureConfigurator();
    fixtureDescriptor.attributes().forEach(
        each -> fixtureConfigurator.configure(
            each.target,
            each.attribute,
            each.resolver
        )
    );
    fixtureDescriptor.operatorFactoryAdders().forEach(
        each -> each.accept(fixtureConfigurator)
    );
    return fixtureConfigurator.build();
  }

  private static Policy createPolicy(FixtureDescriptor fixtureDescriptor) {
    Policy.Builder policyBuilder = new Policy.Builder().setProfile(
        fixtureDescriptor.profile()
    );
    fixtureDescriptor.specs().forEach(policyBuilder::addComponentSpec);
    policyBuilder.setFloorPlan(createFloorPlan(fixtureDescriptor));
    return policyBuilder.build();
  }

  private static FloorPlan createFloorPlan(FixtureDescriptor fixtureDescriptor) {
    FloorPlan floorPlan = new FloorPlan.Impl();
    fixtureDescriptor.refs().forEach(floorPlan::add);
    fixtureDescriptor.wires().forEach(each -> floorPlan.wire(each.from, each.as, each.tos));
    return floorPlan;
  }
}
