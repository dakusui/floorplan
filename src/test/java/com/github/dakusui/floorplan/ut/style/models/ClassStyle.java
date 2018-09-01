package com.github.dakusui.floorplan.ut.style.models;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;

import java.util.Map;
import java.util.function.Supplier;

import static com.github.dakusui.floorplan.ut.style.models.ClassStyle.Attr.NAME;

@SuppressWarnings("unchecked")
public class ClassStyle<A extends ClassStyle.Attr> extends Component.Impl<A> {
  public static final ComponentSpec<Attr> SPEC = ComponentSpec.create(Class.class.cast(ClassStyle.class), Attr.class);

  public interface Attr extends Attribute {
    Attr NAME = Attribute.create(SPEC.property(String.class).required().$());
  }

  @SuppressWarnings({ "unchecked", "WeakerAccess" })
  public ClassStyle(Ref ref, Map<Attr, Supplier<Object>> values, Map<Ref, Component<?>> pool) {
    super(ref, (Map<A, Supplier<Object>>) values, pool);
  }

  @SuppressWarnings("unchecked")
  public String name() {
    return this.valueOf((A) NAME);
  }
}
