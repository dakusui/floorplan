package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;

public interface Sandbox extends Attribute {
  ComponentSpec<Sandbox> SPEC = new ComponentSpec.Builder<>(Sandbox.class).build();

  Sandbox NAME = Attribute.create(SPEC.property(String.class).$());
}
