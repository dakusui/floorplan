package com.github.dakusui.floorplan;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;

import java.util.List;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface TestSuite {
  String getName();

  int size();

  String getNameFor(int i);

  Action setUpFirstTime(Context context);

  Action setUp(Context context, int i);

  Action test(Context context, int i);

  Action tearDown(Context context, int i);

  Action tearDownLastTime(Context context);

  interface Factory {
    TestSuite create(Profile profile);

    abstract class Base<P extends FloorPlan<P>, F extends Fixture> implements Factory {
      @SuppressWarnings("unchecked")
      public TestSuite create(Profile profile) {
        F fixture = (F) addComponentSpecsTo(
            allKnownComponentSpecs(),
            new Policy.Builder()
                .setFloorPlan(buildFloorPlan())
                .setProfile(requireNonNull(profile))
                .setFixtureFactory(createFixtureFactory())
        ).build().fixtureConfigurator().build();
        return new TestSuite() {
          @Override
          public Action setUpFirstTime(Context context) {
            return createActionForSetUpFirstTime(context, fixture);
          }

          @Override
          public Action setUp(Context context, int i) {
            return createActionForSetUp(i, context, fixture);
          }

          @Override
          public Named test(Context context, int i) {
            return createActionForTest(i, context, fixture);
          }

          @Override
          public int size() {
            return numTests();
          }

          @Override
          public String getNameFor(int i) {
            return nameFor(i);
          }

          @Override
          public String getName() {
            return name();
          }

          @Override
          public Action tearDown(Context context, int i) {
            return createActionForTearDown(i, context, fixture);
          }

          @Override
          public Action tearDownLastTime(Context context) {
            return createActionForTearDownLastTime(context, fixture);
          }
        };
      }

      protected abstract String name();

      protected abstract String nameFor(int i);

      protected abstract int numTests();

      protected abstract P buildFloorPlan();

      protected abstract Fixture.Factory<F> createFixtureFactory();

      protected abstract List<ComponentSpec<?>> allKnownComponentSpecs();

      protected abstract Action createActionForSetUp(int i, Context context, F fixture);

      protected abstract Action createActionForSetUpFirstTime(Context context, F fixture);

      protected abstract Named createActionForTest(int i, Context context, F fixture);

      protected abstract Action createActionForTearDown(int i, Context context, F fixture);

      protected abstract Action createActionForTearDownLastTime(Context context, F fixture);

      private Policy.Builder addComponentSpecsTo(List<ComponentSpec<?>> specs, Policy.Builder policyBuilder) {
        specs.forEach(policyBuilder::addComponentSpec);
        return policyBuilder;
      }
    }
  }

}
