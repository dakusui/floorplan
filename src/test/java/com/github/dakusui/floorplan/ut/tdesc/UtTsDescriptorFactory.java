package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.floorplan.component.Ref.ref;

public class UtTsDescriptorFactory extends TestSuiteDescriptor.Factory.Base
    implements TestSuiteDescriptor.Factory {

  @Override
  protected String name() {
    return "UtTsDesc";
  }

  @Override
  protected String testCaseNameFor(int testCaseId) {
    return String.format("UtTsDescCase[%02d]", testCaseId);
  }

  @Override
  protected String testOracleNameFor(int testOracleId) {
    return String.format("UtTsDescOracle[%02d]", testOracleId);
  }

  @Override
  protected int numTests() {
    return 2;
  }

  @Override
  protected int numTestOracles() {
    return 1;
  }

  @Override
  protected Action createActionForSetUp(int testCaseId, Fixture fixture) {
    return nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Fixture fixture) {
    return nop();
  }

  @Override
  protected Action createActionForTest(int testCaseId, int testOracleId, Fixture fixture) {
    return nop();
  }

  @Override
  protected Action createActionForTearDown(int testCaseId, Fixture fixture) {
    return nop();
  }

  @Override
  protected Action createActionForTearDownLastTime(Fixture fixture) {
    return nop();
  }

  @Override
  protected FixtureDescriptor buildFixtureDescriptor(FixtureDescriptor.Builder fixtureDescriptorBuilder) {
    return fixtureDescriptorBuilder.add(ref(UtComponent.SPEC, "1")).build();
  }

  @Override
  protected Predicate<Profile> profileRequirement() {
    return v -> true;
  }
}
