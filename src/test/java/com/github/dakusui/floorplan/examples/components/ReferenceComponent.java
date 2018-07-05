package com.github.dakusui.floorplan.examples.components;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.resolver.Resolvers;

import static com.github.dakusui.floorplan.resolver.Resolvers.nothing;
import static com.github.dakusui.floorplan.resolver.Resolvers.referenceTo;

/**
 * A component definition example that utilizes 'external' reference, i.e.,
 * a reference to another component instance.
 */
public class ReferenceComponent {
  public enum Attr implements Attribute {
    REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE(SPEC.property(AttributeBundle.class).defaultsTo(nothing()).$()),
    REFERENCE_TO_ATTRIBUTE(SPEC.property(String.class).defaultsTo(Resolvers.attributeValueOf(SimpleComponent.Attr.INSTANCE_NAME, referenceTo(REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE))).$());

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

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<Attr>(
      ReferenceComponent.class.getSimpleName(),
      Attr.class
  ).setOperator(
      Operation.INSTALL, Operator.nop()
  ).build();

}
