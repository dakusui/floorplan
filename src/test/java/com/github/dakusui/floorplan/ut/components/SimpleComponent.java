package com.github.dakusui.floorplan.ut.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;

import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.resolver.Resolvers.referenceTo;

public interface SimpleComponent extends Attribute {
  ComponentSpec<SimpleComponent> SPEC                          = new ComponentSpec.Builder<>(
      SimpleComponent.class
  ).build();
  SimpleComponent                INSTANCE_NAME                 = Attribute.create(SPEC.property(String.class).required().$());
  SimpleComponent                DEFAULT_TO_IMMEDIATE          = Attribute.create(SPEC.property(String.class).defaultsTo(immediate("default-value")).$());
  SimpleComponent                DEFAULT_TO_INTERNAL_REFERENCE = Attribute.create(SPEC.property(String.class).defaultsTo(referenceTo(INSTANCE_NAME)).$());
}
