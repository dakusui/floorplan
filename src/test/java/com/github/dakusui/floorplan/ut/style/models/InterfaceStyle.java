package com.github.dakusui.floorplan.ut.style.models;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;

import static com.github.dakusui.floorplan.ut.style.models.InterfaceStyle.Attr.NAME;

public interface InterfaceStyle<A extends InterfaceStyle.Attr> extends Component<A> {
  @SuppressWarnings("unchecked")
  ComponentSpec<Attr> SPEC = ComponentSpec.create(Class.class.cast(InterfaceStyle.class), Attr.class);

  interface Attr extends Attribute {
    Attr NAME = Attribute.create(SPEC.property(String.class).required().$());
  }

  @SuppressWarnings("unchecked")
  default String name() {
    return this.valueOf((A) NAME);
  }

}
