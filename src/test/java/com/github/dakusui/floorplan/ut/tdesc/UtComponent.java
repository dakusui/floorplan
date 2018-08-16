package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.resolver.Resolvers;

public interface UtComponent extends Attribute {
  ComponentSpec<UtComponent> SPEC = new ComponentSpec.Builder<>(UtComponent.class).build();

  UtComponent NAME = Attribute.create(SPEC.property(String.class).defaultsTo(Resolvers.immediate("helloUtComponent")).$());
}
