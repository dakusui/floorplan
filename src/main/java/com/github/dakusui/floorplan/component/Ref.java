package com.github.dakusui.floorplan.component;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class Ref {
  private ComponentSpec<?> spec;
  private String           id;

  private Ref(ComponentSpec spec, String id) {
    this.spec = requireNonNull(spec);
    this.id = requireNonNull(id);
  }

  public ComponentSpec<?> spec() {
    return this.spec;
  }

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

  public static Ref ref(ComponentSpec<?> spec, String id) {
    return new Ref(spec, id);
  }
}
