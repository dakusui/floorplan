package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.resolver.Resolver;

import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class BookstoreApp {
  public enum Attr implements Attribute {
    WEBSERVER(SPEC.property(Configurator.class).defaultsTo(nothing()).$()),
    DBSERVER(SPEC.property(Configurator.class).defaultsTo(nothing()).$()),
    WEBSERVER_ENDPOINT(SPEC.property(String.class).defaultsTo(
        Resolver.of(
            a -> c -> p -> {
              Configurator webserver = (Configurator) c.resolverFor(WEBSERVER, p).apply(a, c, p);
              return null;
            },
            () -> ""
        )
    ).$()),
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(int.class).defaultsTo(immediate(5432)).$()),
    DATADIR(SPEC.property(String.class).defaultsTo(immediate("/var/postgresql/data")).$());
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
