package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Operator;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.Utils;

import java.util.List;

import static com.github.dakusui.floorplan.resolver.Resolvers.listOf;
import static com.github.dakusui.floorplan.resolver.Resolvers.nothing;
import static com.github.dakusui.floorplan.resolver.Resolvers.slotValue;

public class Nginx {
  public enum Attr implements Attribute {
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(Integer.class).defaultsTo(slotValue("port")).$()),
    TARGET_APP(SPEC.property(Configurator.class).defaultsTo(nothing()).$()),
    @SuppressWarnings("unchecked")
    UPSTREAM(SPEC.property(List.class).defaultsTo(listOf(Configurator.class)).$()),
    APP_URL(SPEC.property(String.class).defaultsTo(
        Resolver.of(
            a -> c -> p -> {
              Configurator<BookstoreApp.Attr> app = Utils.resolve(Nginx.Attr.APP_URL, c, p);
              return null;
            },
            () -> "An endpoint to access this application"
        )
    ).$());
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
      Attr.class
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.INSTALL,
          component -> Context::nop
      )
  ).build();
}
