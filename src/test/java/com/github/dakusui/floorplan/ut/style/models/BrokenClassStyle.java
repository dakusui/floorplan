package com.github.dakusui.floorplan.ut.style.models;

import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;

import java.util.Map;

public class BrokenClassStyle extends ClassStyle {
  public static final ComponentSpec<Attr> SPEC = ComponentSpec.create(Class.class.cast(BrokenClassStyle.class), Attr.class);

  public BrokenClassStyle(Ref ref, Map values, Map pool) throws InstantiationException {
    super(ref, values, pool);
    throw new InstantiationException();
  }
}
