package com.github.dakusui.floorplan.ut.style;

import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Ref;

import java.util.Map;

public class ClassStyle extends Component.Impl<InterfaceStyle.Attr> implements InterfaceStyle {
  public ClassStyle(Ref ref, Map<Attr, Object> values, Map<Ref, Component<?>> pool) {
    super(ref, values, pool);
  }
}
