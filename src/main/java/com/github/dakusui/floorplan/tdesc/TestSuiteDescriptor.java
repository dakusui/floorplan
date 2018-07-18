package com.github.dakusui.floorplan.tdesc;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;

import java.util.List;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public interface TestSuiteDescriptor {
  String getName();

  int size();

  int numTestOracles();

  String getTestCaseNameFor(int testCaseId);

  String getTestOracleNameFor(int testOracleId);

  Named setUpFirstTime(Context context);

  Named setUp(Context context, int testCaseId);

  Named test(Context context, int testCaseId, int testOracleId);

  Named tearDown(Context context, int testCaseId);

  Named tearDownLastTime(Context context);

  interface Factory {
    TestSuiteDescriptor create(Profile profile);

    abstract class Base implements Factory {
      @SuppressWarnings("unchecked")
      public TestSuiteDescriptor create(Profile profile) {
        Fixture fixture = configureFixture(
            addComponentSpecsTo(
                allKnownComponentSpecs(),
                new Policy.Builder()
            ).setFloorPlan(
                createFloorPlan()
            ).setProfile(
                requireNonNull(profile)
            ).setFixtureFactory(
                createFixtureFactory()
            ).build().fixtureConfigurator()
        ).build();
        return new TestSuiteDescriptor() {
          @Override
          public Named setUpFirstTime(Context context) {
            return (Named) context.named(
                "BEFORE ALL",
                createActionForSetUpFirstTime(context, fixture)
            );
          }

          @Override
          public Named setUp(Context context, int testCaseId) {
            return (Named) context.named(
                String.format("BEFORE:%s", getTestCaseNameFor(testCaseId)),
                createActionForSetUp(testCaseId, context, fixture)
            );
          }

          @Override
          public Named test(Context context, int testCaseId, int testOracleId) {
            return (Named) context.named(
                String.format("TEST:%s.%s", getTestOracleNameFor(testOracleId), getTestCaseNameFor(testCaseId)),
                createActionForTest(testCaseId, testOracleId, context, fixture)
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
          public String getTestCaseNameFor(int testCaseId) {
            return testCaseNameFor(testCaseId);
          }

          @Override
          public String getTestOracleNameFor(int testOracleId) {
            return testOracleNameFor(testOracleId);
          }

          @Override
          public String getName() {
            return name();
          }

          @Override
          public Named tearDown(Context context, int testCaseId) {
            return (Named) context.named(
                String.format("AFTER:%s", getTestCaseNameFor(testCaseId)),
                createActionForTearDown(testCaseId, context, fixture)
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

      private Fixture.Factory createFixtureFactory() {
        return (policy, fixtureConfigurator) -> new Fixture.Impl(policy, configureFixture(fixtureConfigurator));
      }

      private FloorPlan createFloorPlan() {
        return configureFloorPlan(new FloorPlan.Impl());
      }

      protected abstract String name();

      protected abstract String testCaseNameFor(int testCaseId);

      protected abstract String testOracleNameFor(int testOracleId);

      protected abstract int numTests();

      protected abstract int numOracles();

      protected abstract FixtureConfigurator configureFixture(FixtureConfigurator fixtureConfigurator);

      protected abstract List<ComponentSpec<?>> allKnownComponentSpecs();

      protected abstract Action createActionForSetUp(int testCaseId, Context context, Fixture fixture);

      protected abstract Action createActionForSetUpFirstTime(Context context, Fixture fixture);

      protected abstract Action createActionForTest(int testCaseId, int testOracleId, Context context, Fixture fixture);

      protected abstract Action createActionForTearDown(int testCaseId, Context context, Fixture fixture);

      protected abstract Action createActionForTearDownLastTime(Context context, Fixture fixture);

      protected abstract FloorPlan configureFloorPlan(FloorPlan floorPlan);

      private Policy.Builder addComponentSpecsTo(List<ComponentSpec<?>> specs, Policy.Builder policyBuilder) {
        specs.forEach(policyBuilder::addComponentSpec);
        return policyBuilder;
      }
    }
  }
}
