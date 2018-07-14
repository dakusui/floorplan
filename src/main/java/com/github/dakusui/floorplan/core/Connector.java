package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;

import static com.github.dakusui.floorplan.utils.Checks.requireArgument;
import static java.util.Objects.requireNonNull;

public class Connector {
  private Attribute fromAttr;
  private Ref       from;

  private Connector(Ref from, Attribute fromAttr) {
    requireArgument(fromAttr, attr -> from.spec() == attr.spec());
    this.fromAttr = requireNonNull(fromAttr);
    this.from = requireNonNull(from);
  }

  @Override
  public int hashCode() {
    return this.from.hashCode();
  }

  @Override
  public boolean equals(Object anotherObject) {
    if (anotherObject instanceof Connector) {
      Connector another = Connector.class.cast(anotherObject);
      return another.from.equals(this.from) && another.fromAttr.equals(this.fromAttr);
    }
    return false;
  }

  static Connector connector(Ref from, Attribute fromAttr) {
    return new Connector(from, fromAttr);
  }
}
