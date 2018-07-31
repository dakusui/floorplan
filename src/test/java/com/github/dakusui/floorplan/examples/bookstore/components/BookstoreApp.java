package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.Utils;

import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.runShell;

/**
 * A class to define a deployment specification of an internet bookstore
 * application.
 */
public class BookstoreApp {
  public enum Attr implements Attribute {
    APPNAME(SPEC.property(String.class).defaultsTo(immediate("bookstore")).$()),
    WEBSERVER(SPEC.property(Apache.SPEC).defaultsTo(nothing()).$()),
    WEBSERVER_HOST(SPEC.property(String.class).defaultsTo(attributeValueOf(Apache.Attr.HOSTNAME, referenceTo(WEBSERVER))).$()),
    WEBSERVER_PORT(SPEC.property(Integer.class).defaultsTo(attributeValueOf(Apache.Attr.PORTNUMBER, referenceTo(WEBSERVER))).$()),
    DBSERVER(SPEC.property(PostgreSQL.SPEC).defaultsTo(nothing()).$()),
    @SuppressWarnings("unchecked")
    DBSERVER_ENDPOINT(SPEC.property(String.class).defaultsTo(
        Resolver.of(
            a -> c -> p -> {
              Configurator<PostgreSQL.Attr> dbServer = p.lookUp(Utils.resolve(DBSERVER, c, p));
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
    ENDPOINT(SPEC.property(String.class).defaultsTo(
        Resolver.of(
            a -> c -> p -> {
              Configurator<Apache.Attr> webServer = p.fixtureConfigurator().lookUp(Utils.resolve(WEBSERVER, c, p));
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
                runShell(
                    "scp -r ~/apps/%s root@%s:%s/%s",
                    component.valueOf(Attr.APPNAME),
                    component.valueOf(Attr.WEBSERVER_HOST),
                    component.<Component<Apache.Attr>>valueOf(Attr.WEBSERVER).valueOf(Apache.Attr.DATADIR),
                    component.valueOf(Attr.APPNAME)
                );
              }),
              $.simple("Modify database server location", () -> {
                runShell(
                    "ssh -l root@%s sed -i /etc/bookstoreapp.conf 's!dbms=__DBMS__!dbms=%s!g'",
                    component.valueOf(Attr.WEBSERVER_HOST),
                    component.valueOf(Attr.DBSERVER_ENDPOINT)
                );
              })
          )
      )
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.UNINSTALL,
          attrComponent -> $ -> $.named("Do something for uninstallation", $.nop())
      )
  ).build();
}
