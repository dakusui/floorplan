package com.github.dakusui.floorplan.component;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A class that represents a reference to a component instance or a configurator.
 * A {@code Ref} class object consists of two elements which are {@code spec} and
 * {@code id}. {@code spec} is a specification of an entity that is referenced to
 * by this object. And {@code id} identifies such an entity among all component
 * instances or configurators that belongs to the same {@code spec}.
 * <p>
 * If a couple of {@code Ref} objects are given and their {@code spec}s and {@code id}s
 * are equal to each other's respectively, those two objects are considered equal.
 *
 * @see Configurator
 * @see ComponentSpec
 * @see Ref#ref(ComponentSpec, String)
 */
public final class Ref {
  private ComponentSpec<?> spec;
  private String           id;

  private Ref(ComponentSpec spec, String id) {
    this.spec = requireNonNull(spec);
    this.id = requireNonNull(id);
  }

  /**
   * Returns a specification object ({@code ComponentSpec}) of this object.
   *
   * @param <A> A type of attribute that describes the spec.
   * @return A component's spec.
   */
  @SuppressWarnings("unchecked")
  public <A extends Attribute> ComponentSpec<? extends A> spec() {
    return (ComponentSpec<A>) this.spec;
  }

  /**
   * Returns an identifier of the component instance or the configurator.
   *
   * @return An identifier.
   */
  public String id() {
    return this.id;
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  @Override
  public boolean equals(Object anotherObject) {
    if (anotherObject instanceof Ref) {
      Ref another = (Ref) anotherObject;
      return this.spec.equals(another.spec) && Objects.equals(this.id, another.id);
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("%s#%s", spec, id);
  }

  /**
   * Creates a reference to a specified entity (a component instance or a configurator).
   *
   * @param spec A specification of a referenced entity
   * @param id   An identifier of a referenced entity
   * @return A created instance
   */
  public static Ref ref(ComponentSpec<?> spec, String id) {
    return new Ref(spec, id);
  }
}
