package com.github.dakusui.floorplan.ut.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.resolver.Resolvers;

import static com.github.dakusui.floorplan.resolver.Resolvers.referenceTo;

/**
 * A component definition example that utilizes 'external' reference, i.e.,
 * a reference to another component instance.
 */
public interface ReferenceComponent extends Attribute {
  ComponentSpec<ReferenceComponent> SPEC                                    = new ComponentSpec.Builder<>(
      ReferenceComponent.class
  ).build();
  ReferenceComponent                REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE = Attribute.create(SPEC.property(Ref.class).required().$());
  ReferenceComponent                REFERENCE_TO_ATTRIBUTE                  = Attribute.create(SPEC.property(String.class).defaultsTo(
      Resolvers.attributeValueOf(SimpleComponent.INSTANCE_NAME, referenceTo(REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE)
      )).$());
}
