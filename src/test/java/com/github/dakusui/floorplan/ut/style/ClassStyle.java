package com.github.dakusui.floorplan.ut.style;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;

import java.util.Map;

import static com.github.dakusui.floorplan.ut.style.ClassStyle.Attr.NAME;

public class ClassStyle extends Component.Impl<ClassStyle.Attr> {
  public static final ComponentSpec<Attr> SPEC = ComponentSpec.create(ClassStyle.class, Attr.class);

  public interface Attr extends Attribute {
    Attr NAME = Attribute.create(SPEC.property(String.class).required().$());
  }

  public ClassStyle(Ref ref, Map<Attr, Object> values, Map<Ref, Component<?>> pool) {
    super(ref, values, pool);
  }

  public String name() {
    return this.valueOf(NAME);
  }
}
