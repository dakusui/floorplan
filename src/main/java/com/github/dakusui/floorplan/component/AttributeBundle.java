package com.github.dakusui.floorplan.component;

public interface AttributeBundle<A extends Attribute> {
  Ref ref();

  ComponentSpec<A> spec();

}
