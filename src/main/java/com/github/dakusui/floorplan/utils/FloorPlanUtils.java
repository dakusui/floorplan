package com.github.dakusui.floorplan.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * A utility class that collects useful methods for users of the 'FloorPlan' library.
 */
public enum FloorPlanUtils {
  ;

  public static Action createGroupedAction(
      Context context,
      boolean parallel,
      Function<Component<?>, Component.ActionFactory> actionFactoryCreator,
      Fixture fixture,
      Ref... refs
  ) {
    Action[] actions = Arrays.stream(
        refs
    ).map(
        fixture::lookUp
    ).map(
        actionFactoryCreator::apply
    ).map(
        actionFactory -> actionFactory.apply(context)
    ).collect(
        toList()
    ).toArray(
        new Action[refs.length]
    );
    return parallel ?
        context.concurrent(actions) :
        context.sequential(actions);
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute, T> T resolve(A attr, Configurator<A> configurator, Policy policy) {
    return (T) Function.class.cast(Function.class.cast(configurator.resolverFor(attr, policy).<A>apply(attr)).apply(configurator)).apply(policy);
  }
}
