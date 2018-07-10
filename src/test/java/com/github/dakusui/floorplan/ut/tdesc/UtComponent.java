package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.resolver.Resolvers;

public class UtComponent {
  public enum Attr implements Attribute {
    NAME(SPEC.property(String.class).defaultsTo(Resolvers.immediate("helloUtComponent")).$());

    final private Bean<Attr> bean;

    Attr(Bean<Attr> bean) {
      this.bean = bean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Bean<Attr> bean() {
      return this.bean;
    }
  }

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
}
