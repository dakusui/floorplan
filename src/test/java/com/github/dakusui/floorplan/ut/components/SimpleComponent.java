package com.github.dakusui.floorplan.ut.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;

import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class SimpleComponent {
  public enum Attr implements Attribute {
    INSTANCE_NAME(SPEC.property(String.class).defaultsTo(nothing()).$()),
    DEFAULT_TO_IMMEDIATE(SPEC.property(String.class).defaultsTo(immediate("default-value")).$()),
    DEFAULT_TO_INTERNAL_REFERENCE(SPEC.property(String.class).defaultsTo(referenceTo(INSTANCE_NAME)).$()),;
    private final Bean<Attr> bean;

    Attr(Bean<Attr> bean) {
      this.bean = bean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Bean<Attr> bean() {
      return this.bean;
    }
  }

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(
      Attr.class
  ).build();
}
