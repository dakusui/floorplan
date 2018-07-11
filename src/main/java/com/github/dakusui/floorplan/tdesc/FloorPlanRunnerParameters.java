package com.github.dakusui.floorplan.tdesc;

import com.github.dakusui.floorplan.policy.Profile;

public @interface FloorPlanRunnerParameters {
  Class<? extends TestSuiteDescriptor.Factory> descriptorFactory();
  Class<? extends Profile.Factory> profileFactory();
}
