package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;

import java.util.List;

import static com.github.dakusui.floorplan.component.Ref.ref;
import static java.util.Collections.singletonList;

public class UtTsDescriptorFactory extends TestSuiteDescriptor.Factory.Base
    implements TestSuiteDescriptor.Factory {
  @Override
  protected String name() {
    return "UtTsDesc";
  }

  @Override
  protected String testCaseNameFor(int i) {
    return String.format("UtTsDescCase[%02d]", i);
  }

  @Override
  protected String testOracleNameFor(int j) {
    return String.format("UtTsDescOracle[%02d]", j);
  }

  @Override
  protected int numTests() {
    return 2;
  }

  @Override
  protected int numOracles() {
    return 1;
  }

  @Override
  protected Fixture.Factory createFixtureFactory() {
    return UtFixture::new;
  }

  @Override
  protected List<ComponentSpec<?>> allKnownComponentSpecs() {
    return singletonList(UtComponent.SPEC);
  }

  @Override
  protected Action createActionForSetUp(int i, Context context, Fixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Context context, Fixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForTest(int i, int j, Context context, Fixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForTearDown(int i, Context context, Fixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForTearDownLastTime(Context context, Fixture fixture) {
    return context.nop();
  }

  @Override
  protected FloorPlan configureFloorPlan(FloorPlan floorPlan) {
    return floorPlan.add(ref(UtComponent.SPEC, "1"));
  }
}
