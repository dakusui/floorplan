package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Ref;

import static com.github.dakusui.floorplan.component.Ref.ref;

public class UtTsDescFloorPlan extends FloorPlan.Base<UtTsDescFloorPlan> {
  public final Ref c1 = ref(UtComponent.SPEC, "1");
}
