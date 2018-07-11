package com.github.dakusui.floorplan.tdesc;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;

import java.util.List;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface TestSuiteDescriptor {
  String getName();

  int size();

  int numTestOracles();

  String getTestCaseNameFor(int i);

  String getTestOracleNameFor(int j);

  Named setUpFirstTime(Context context);

  Named setUp(Context context, int i);

  Named test(Context context, int i, int j);

  Named tearDown(Context context, int i);

  Named tearDownLastTime(Context context);

  interface Factory<P extends FloorPlan> {
    TestSuiteDescriptor create(Profile profile);

    P floorPlan();

    abstract class Base<P extends FloorPlan, F extends Fixture> implements Factory<P> {
      private final P floorPlan = buildFloorPlan();

      @SuppressWarnings("unchecked")
      public TestSuiteDescriptor create(Profile profile) {
        F fixture = (F) addComponentSpecsTo(
            allKnownComponentSpecs(),
            new Policy.Builder()
        ).setFloorPlan(
            floorPlan
        ).setProfile(
            requireNonNull(profile)
        ).setFixtureFactory(
            createFixtureFactory()
        ).build().fixtureConfigurator().build();
        return new TestSuiteDescriptor() {
          @Override
          public Named setUpFirstTime(Context context) {
            return (Named) context.named(
                "BEFORE ALL",
                createActionForSetUpFirstTime(context, fixture)
            );
          }

          @Override
          public Named setUp(Context context, int i) {
            return (Named) context.named(
                String.format("BEFORE:%s", getTestCaseNameFor(i)),
                createActionForSetUp(i, context, fixture)
            );
          }

          @Override
          public Named test(Context context, int i, int j) {
            return (Named) context.named(
                String.format("TEST:%s.%s", getTestOracleNameFor(j), getTestCaseNameFor(i)),
                createActionForTest(i, j, context, fixture)
            );
          }

          @Override
          public int size() {
            return numTests();
          }

          @Override
          public int numTestOracles() {
            return numOracles();
          }

          @Override
          public String getTestCaseNameFor(int i) {
            return testCaseNameFor(i);
          }

          @Override
          public String getTestOracleNameFor(int j) {
            return testOracleNameFor(j);
          }

          @Override
          public String getName() {
            return name();
          }

          @Override
          public Named tearDown(Context context, int i) {
            return (Named) context.named(
                String.format("AFTER:%s", getTestCaseNameFor(i)),
                createActionForTearDown(i, context, fixture)
            );
          }

          @Override
          public Named tearDownLastTime(Context context) {
            return (Named) context.named("AFTER ALL",
                createActionForTearDownLastTime(context, fixture)
            );
          }

          @Override
          public String toString() {
            return String.format("%s(%s[testcases])", this.getName(), size());
          }
        };
      }

      @Override
      public P floorPlan() {
        return this.floorPlan;
      }

      protected abstract String name();

      protected abstract String testCaseNameFor(int i);

      protected abstract String testOracleNameFor(int j);

      protected abstract int numTests();

      protected abstract int numOracles();

      protected abstract P buildFloorPlan();

      protected abstract Fixture.Factory<F> createFixtureFactory();

      protected abstract List<ComponentSpec<?>> allKnownComponentSpecs();

      protected abstract Action createActionForSetUp(int i, Context context, F fixture);

      protected abstract Action createActionForSetUpFirstTime(Context context, F fixture);

      protected abstract Action createActionForTest(int i, int j, Context context, F fixture);

      protected abstract Action createActionForTearDown(int i, Context context, F fixture);

      protected abstract Action createActionForTearDownLastTime(Context context, F fixture);

      private Policy.Builder addComponentSpecsTo(List<ComponentSpec<?>> specs, Policy.Builder policyBuilder) {
        specs.forEach(policyBuilder::addComponentSpec);
        return policyBuilder;
      }
    }
  }
}
