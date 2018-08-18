package com.github.dakusui.floorplan.ut.style;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;

import static com.github.dakusui.floorplan.ut.style.InterfaceStyle.Attr.NAME;

public interface InterfaceStyle extends Component<InterfaceStyle.Attr> {
  ComponentSpec<Attr> SPEC = ComponentSpec.create(ClassStyle.class, Attr.class);

  interface Attr extends Attribute {
    Attr NAME = Attribute.create(SPEC.property(String.class).required().$());
  }

  default String name() {
    return this.valueOf(NAME);
  }

}
