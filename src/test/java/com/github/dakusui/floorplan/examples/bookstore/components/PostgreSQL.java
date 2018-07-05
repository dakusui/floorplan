package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Operation;
import com.github.dakusui.floorplan.component.Operator;

import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.resolver.Resolvers.slotValue;

public class PostgreSQL {
  public enum Attr implements Attribute {
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(int.class).defaultsTo(immediate(5432)).$()),
    DATADIR(SPEC.property(String.class).defaultsTo(immediate("/var/postgresql/data")).$())
    ;
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
      PostgreSQL.class.getSimpleName(),
      Attr.class
  ).setOperator(
      Operation.INSTALL, Operator.nop()
  ).build();
}
