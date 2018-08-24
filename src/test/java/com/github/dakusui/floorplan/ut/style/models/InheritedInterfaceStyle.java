package com.github.dakusui.floorplan.ut.style.models;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;

import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.referenceTo;
import static com.github.dakusui.floorplan.resolver.Resolvers.transform;

public interface InheritedInterfaceStyle extends InterfaceStyle<InheritedInterfaceStyle.Attr> {
  ComponentSpec<Attr> SPEC = ComponentSpec.create(InheritedInterfaceStyle.class, Attr.class);

  interface Attr extends InterfaceStyle.Attr {
    Attr URL = Attribute.create(SPEC.property(String.class).defaultsTo(
        transform(
            referenceTo(InterfaceStyle.Attr.NAME),
            mapper(v -> String.format("http://localhost:8080/%s", v))
        )).$());
  }

  @Override
  default String name() {
    return "{" + InterfaceStyle.super.name() + "}";
  }

  default String url() {
    return this.valueOf(Attr.URL);
  }
}
