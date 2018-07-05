package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.components.SimpleComponent;
import org.junit.Test;

public class BookstoreExample {
  Ref simple1 = Ref.ref(SimpleComponent.SPEC, "1");

  @Test
  public void example() {

  }

  FloorPlan buildFloorPlan() {
    return new FloorPlan().add(simple1);
  }
}
