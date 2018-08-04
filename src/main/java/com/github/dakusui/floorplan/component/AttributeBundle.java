package com.github.dakusui.floorplan.component;

/**
 * An interface that represents a bundle of attributes.
 *
 * @param <A> A type of attributes, not to be confused of a type of their values.
 */
public interface AttributeBundle<A extends Attribute> {
  /**
   * Returns a reference to  a component instance or configurator whose specification
   * is described by attributes that this object holds.
   *
   * @return reference to an entity that this object describes.
   */
  Ref ref();

  /**
   * A short hand method for {@code ref().spec()}.
   *
   * @return A specification of a component that this object describes.
   */
  @SuppressWarnings("unchecked")
  default ComponentSpec<A> spec() {
    return (ComponentSpec<A>) this.ref().spec();
  }
}
