package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.runShell;

/**
 * A class to define a deployment specification of an internet bookstore
 * application.
 */
public class BookstoreApp {
  public interface Attr extends Attribute {
    Attr APPNAME = Attribute.create(SPEC.property(String.class).defaultsTo(immediate("bookstore")).$());
    Attr WEBSERVER = Attribute.create((SPEC.property(Apache.SPEC).required().$()));
    Attr WEBSERVER_HOST = Attribute.create((SPEC.property(String.class).defaultsTo(attributeValueOf(Apache.Attr.HOSTNAME, referenceTo(WEBSERVER))).$()));
    Attr WEBSERVER_PORT = Attribute.create((SPEC.property(Integer.class).defaultsTo(attributeValueOf(Apache.Attr.PORTNUMBER, referenceTo(WEBSERVER))).$()));
    Attr DBSERVER = Attribute.create((SPEC.property(PostgreSQL.SPEC).required().$()));
    @SuppressWarnings("unchecked")
    Attr DBSERVER_ENDPOINT = Attribute.create((SPEC.property(String.class).defaultsTo(
        Resolver.of(
            c -> p -> {
              Configurator<PostgreSQL.Attr> dbServer = p.lookUp(FloorPlanUtils.resolve(DBSERVER, c, p));
              return String.format(
                  "jdbc:postgresql://%s:%s/%s",
                  FloorPlanUtils.resolve(PostgreSQL.Attr.HOSTNAME, dbServer, p),
                  FloorPlanUtils.resolve(PostgreSQL.Attr.PORTNUMBER, dbServer, p),
                  FloorPlanUtils.resolve(PostgreSQL.Attr.BOOKSTORE_DATABASE, dbServer, p)
              );
            },
            () -> "An endpoint to access a database server where data of this application is stored."
        )
    ).$()));
    Attr ENDPOINT = Attribute.create((SPEC.property(String.class).defaultsTo(
        Resolver.of(
            c -> p -> {
              Configurator<Apache.Attr> webServer = p.fixtureConfigurator().lookUp(FloorPlanUtils.resolve(WEBSERVER, c, p));
              return String.format(
                  "http://%s:%s/%s",
                  FloorPlanUtils.resolve(Apache.Attr.HOSTNAME, webServer, p),
                  FloorPlanUtils.resolve(Apache.Attr.PORTNUMBER, webServer, p),
                  FloorPlanUtils.resolve(APPNAME, c, p)
              );
            },
            () -> "An endpoint to access this application"
        )
    ).$()));
  }

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(
      Attr.class
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.INSTALL,
          component -> sequential(
              simple("Deploy files under apache httpd", (c) -> runShell(
                  "scp -r ~/apps/%s root@%s:%s/%s",
                  component.valueOf(Attr.APPNAME),
                  component.valueOf(Attr.WEBSERVER_HOST),
                  component.<Component<Apache.Attr>>valueOf(Attr.WEBSERVER).valueOf(Apache.Attr.DATADIR),
                  component.valueOf(Attr.APPNAME)
              )),
              simple("Modify database server location", (c) -> runShell(
                  "ssh -l root@%s sed -i /etc/bookstoreapp.conf 's!dbms=__DBMS__!dbms=%s!g'",
                  component.valueOf(Attr.WEBSERVER_HOST),
                  component.valueOf(Attr.DBSERVER_ENDPOINT)
              ))))
  ).addOperatorFactory(
      Operator.Factory.of(
          Operator.Type.UNINSTALL,
          attrComponent -> named("Do something for uninstallation", nop())
      )
  ).build();
}
