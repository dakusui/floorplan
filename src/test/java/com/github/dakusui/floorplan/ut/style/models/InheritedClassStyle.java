package com.github.dakusui.floorplan.ut.style.models;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;

import java.util.Map;

import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.referenceTo;
import static com.github.dakusui.floorplan.resolver.Resolvers.transform;
import static com.github.dakusui.floorplan.ut.style.models.InheritedClassStyle.Attr.URL;

public class InheritedClassStyle extends ClassStyle<InheritedClassStyle.Attr> {
  public static final ComponentSpec<Attr> SPEC = ComponentSpec.create(InheritedClassStyle.class, Attr.class);

  public InheritedClassStyle(Ref ref, Map<ClassStyle.Attr, Object> values, Map<Ref, Component<?>> pool) {
    super(ref, values, pool);
  }

  public interface Attr extends ClassStyle.Attr {
    Attr URL = Attribute.create(SPEC.property(String.class).defaultsTo(
        transform(
            referenceTo(ClassStyle.Attr.NAME),
            mapper(v -> String.format("http://localhost:8081/%s", v))
        )).$());
  }

  public String name() {
    return "<" + super.name() + ">";
  }

  public String url() {
    return this.valueOf(URL);
  }
}

