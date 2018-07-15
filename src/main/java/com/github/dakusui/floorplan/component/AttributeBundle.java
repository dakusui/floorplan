package com.github.dakusui.floorplan.component;

/**
 * An interface that represents a bundle of attributes.
 *
 * @param <A> A type of attributes, not to be confused of a type of their values.
 */
public interface AttributeBundle<A extends Attribute> {
  Ref ref();

  ComponentSpec<A> spec();
}
