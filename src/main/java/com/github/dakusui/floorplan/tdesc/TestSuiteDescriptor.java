package com.github.dakusui.floorplan.tdesc;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FloorPlanDescriptor;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static com.github.dakusui.floorplan.exception.Exceptions.incompatibleProfile;
import static com.github.dakusui.floorplan.utils.Checks.require;

public interface TestSuiteDescriptor {
  String getName();

  int size();

  int numTestOracles();

  String getTestCaseNameFor(int testCaseId);

  String getTestOracleNameFor(int testOracleId);

  Named setUpFirstTime();

  Named setUp(int testCaseId);

  Named test(int testCaseId, int testOracleId);

  Named tearDown(int testCaseId);

  Named tearDownLastTime();

  interface Factory {
    TestSuiteDescriptor create(Profile profile);

    abstract class Base implements Factory {
      @SuppressWarnings("unchecked")
      public TestSuiteDescriptor create(Profile profile) {
        FloorPlan floorPlan = FloorPlanUtils.buildFloorPlan(
            buildFloorPlanDescriptor(createFloorPlanDescriptorBuilder(profile))
        );

        return new TestSuiteDescriptor() {
          {
            require(profile, profileRequirement(), p -> incompatibleProfile(p, profileRequirement()));
          }

          @Override
          public Named setUpFirstTime() {
            return (Named) named(
                "BEFORE ALL",
                createActionForSetUpFirstTime(floorPlan)
            );
          }

          @Override
          public Named setUp(int testCaseId) {
            return (Named) named(
                String.format("BEFORE:%s", getTestCaseNameFor(testCaseId)),
                createActionForSetUp(testCaseId, floorPlan)
            );
          }

          @Override
          public Named test(int testCaseId, int testOracleId) {
            return (Named) named(
                String.format("TEST:%s.%s", getTestOracleNameFor(testOracleId), getTestCaseNameFor(testCaseId)),
                createActionForTest(testCaseId, testOracleId, floorPlan)
            );
          }

          @Override
          public int size() {
            return numTests();
          }

          @Override
          public int numTestOracles() {
            return Base.this.numTestOracles();
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
          public Named tearDown(int testCaseId) {
            return (Named) named(
                String.format("AFTER:%s", getTestCaseNameFor(testCaseId)),
                createActionForTearDown(testCaseId, floorPlan)
            );
          }

          @Override
          public Named tearDownLastTime() {
            return (Named) named("AFTER ALL",
                createActionForTearDownLastTime(floorPlan)
            );
          }

          public Predicate<Profile> profileRequirement() {
            return Base.this.profileRequirement();
          }

          @Override
          public String toString() {
            return String.format("%s(%s[testcases])", this.getName(), size());
          }
        };
      }

      private FloorPlanDescriptor.Builder createFloorPlanDescriptorBuilder(Profile profile) {
        return new FloorPlanDescriptor.Builder(profile);
      }

      protected abstract FloorPlanDescriptor buildFloorPlanDescriptor(FloorPlanDescriptor.Builder floorPlanDescriptorBuilder);

      protected abstract Predicate<Profile> profileRequirement();

      protected abstract String name();

      protected abstract String testCaseNameFor(int testCaseId);

      protected abstract String testOracleNameFor(int testOracleId);

      protected abstract int numTests();

      protected abstract int numTestOracles();

      protected abstract Action createActionForSetUp(int testCaseId, FloorPlan floorPlan);

      protected abstract Action createActionForSetUpFirstTime(FloorPlan floorPlan);

      protected abstract Action createActionForTest(int testCaseId, int testOracleId, FloorPlan floorPlan);

      protected abstract Action createActionForTearDown(int testCaseId, FloorPlan floorPlan);

      protected abstract Action createActionForTearDownLastTime(FloorPlan floorPlan);
    }
  }
}
