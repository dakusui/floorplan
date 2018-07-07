package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.Utils;

import static com.github.dakusui.floorplan.UtUtils.printf;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;

/**
 * A class to define a deployment specification of an internet bookstore
 * application.
 */
public class BookstoreApp {
  public enum Attr implements Attribute {
    APPNAME(SPEC.property(String.class).defaultsTo(immediate("bookstore")).$()),
    WEBSERVER(SPEC.property(Configurator.class).defaultsTo(nothing()).$()),
    WEBSERVER_HOST(SPEC.property(String.class).defaultsTo(attributeValueOf(Apache.Attr.HOSTNAME, referenceTo(WEBSERVER))).$()),
    DBSERVER(SPEC.property(Configurator.class).defaultsTo(nothing()).$()),
    @SuppressWarnings("unchecked")
    DBSERVER_ENDPOINT(SPEC.property(String.class).defaultsTo(
        Resolver.of(
            a -> c -> p -> {
              Configurator<PostgreSQL.Attr> dbServer = Utils.resolve(DBSERVER, c, p);
              return String.format(
                  "jdbc:postgresql://%s:%s/%s",
                  Utils.resolve(PostgreSQL.Attr.HOSTNAME, dbServer, p),
                  Utils.resolve(PostgreSQL.Attr.PORTNUMBER, dbServer, p),
                  Utils.resolve(PostgreSQL.Attr.BOOKSTORE_DATABASE, dbServer, p)
              );
            },
            () -> "An endpoint to access a database server where data of this application is stored."
        )
    ).$()),
    APP_URL(SPEC.property(String.class).defaultsTo(
        Resolver.of(
            a -> c -> p -> {
              Configurator<Apache.Attr> webServer = Utils.resolve(WEBSERVER, c, p);
              return String.format(
                  "http://%s:%s/%s",
                  Utils.resolve(Apache.Attr.HOSTNAME, webServer, p),
                  Utils.resolve(Apache.Attr.PORTNUMBER, webServer, p),
                  Utils.resolve(APPNAME, c, p)
              );
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
          component -> $ -> $.sequential(
              $.simple("Deploy files under apache httpd", () -> {
                printf(
                    "scp -r ~/apps/%s root@%s:%s/%s",
                    component.valueOf(Attr.APPNAME),
                    component.valueOf(Attr.WEBSERVER_HOST),
                    component.<Component<Apache.Attr>>valueOf(Attr.WEBSERVER).valueOf(Apache.Attr.DATADIR),
                    component.valueOf(Attr.APPNAME)
                );
              }),
              $.simple("Modify database server location", () -> {
                printf(
                    "ssh -l root@%s sed -i /etc/bookstoreapp.conf 's!dbms=__DBMS__!dbms=%s!g'",
                    component.valueOf(Attr.WEBSERVER_HOST),
                    component.valueOf(Attr.DBSERVER_ENDPOINT)
                );
              })
          )
      )
  ).build();
}
