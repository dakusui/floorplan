package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.TestSuiteDescriptor;
import com.github.dakusui.floorplan.component.ComponentSpec;

import java.util.List;

import static java.util.Collections.singletonList;

public class UtTsDescriptorBuilder extends TestSuiteDescriptor.Factory.Base<UtTsDescFloorPlan, UtFixture>
    implements TestSuiteDescriptor.Factory<UtTsDescFloorPlan> {
  @Override
  protected String name() {
    return "UtTsDesc";
  }

  @Override
  protected String nameFor(int i) {
    return String.format("UtTsDesc[%02d]", i);
  }

  @Override
  protected int numTests() {
    return 2;
  }

  @Override
  protected UtTsDescFloorPlan buildFloorPlan() {
    return new UtTsDescFloorPlan();
  }

  @Override
  protected Fixture.Factory<UtFixture> createFixtureFactory() {
    return UtFixture::new;
  }

  @Override
  protected List<ComponentSpec<?>> allKnownComponentSpecs() {
    return singletonList(UtComponent.SPEC);
  }

  @Override
  protected Action createActionForSetUp(int i, Context context, UtFixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Context context, UtFixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForTest(int i, Context context, UtFixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForTearDown(int i, Context context, UtFixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForTearDownLastTime(Context context, UtFixture fixture) {
    return context.nop();
  }
}
