package com.github.dakusui.floorplan.ut.style.models;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.resolver.Resolvers;

import java.util.Map;

import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.transform;

public class BrokenModel extends ClassStyle<BrokenModel.Attr> {
  public static final ComponentSpec<Attr> SPEC = ComponentSpec.create(BrokenModel.class, Attr.class);

  public BrokenModel(Ref ref, Map<ClassStyle.Attr, Object> values, Map<Ref, Component<?>> pool) {
    super(ref, values, pool);
  }

  public interface Attr extends ClassStyle.Attr {
    Attr URL = Attribute.create(SPEC.property(String.class).defaultsTo(
        transform(
            /*
             * InterfaceStyle.Attr.NAME shouldn't be referenced because it is neither
             * a part of 'BrokenModel' nor ClassStyle model.
             */
            Resolvers.referenceTo(InterfaceStyle.Attr.NAME),
            mapper(v -> String.format("http://localhost:8081/%s", v))
        )).$());
  }
}
