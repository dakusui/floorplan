package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Configurator;
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
    ComponentSpec<Attr> SPEC           = new ComponentSpec.Builder<>(
        Attr.class
    ).build();
    Attr                APPNAME        = Attribute.create(SPEC.property(String.class).defaultsTo(immediate("bookstore")).$());
    Attr                WEBSERVER      = Attribute.create((SPEC.property(Apache.SPEC).required().$()));
    Attr                WEBSERVER_HOST = Attribute.create((SPEC.property(String.class).defaultsTo(attributeValueOf(Apache.Attr.HOSTNAME, referenceTo(WEBSERVER))).$()));
    Attr                WEBSERVER_PORT = Attribute.create((SPEC.property(Integer.class).defaultsTo(attributeValueOf(Apache.Attr.PORTNUMBER, referenceTo(WEBSERVER))).$()));
    Attr                DBSERVER       = Attribute.create((SPEC.property(PostgreSQL.SPEC).required().$()));
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
    Attr ENDPOINT  = Attribute.create((SPEC.property(String.class).defaultsTo(
        Resolver.of(
            c -> p -> {
              Configurator<Apache.Attr> webServer = p.floorPlanConfigurator().lookUp(FloorPlanUtils.resolve(WEBSERVER, c, p));
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
    Attr INSTALL   = Attribute.create(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.<BookstoreApp.Attr>of(
            (component) -> sequential(
                simple("Deploy files under apache httpd", (c) -> runShell(
                    "scp -r ~/apps/%s root@%s:%s/%s",
                    component.valueOf(APPNAME),
                    component.valueOf(WEBSERVER_HOST),
                    component.<Component<Apache.Attr>>valueOf(WEBSERVER).valueOf(Apache.Attr.DATADIR),
                    component.valueOf(APPNAME)
                )),
                simple("Modify database server location", (c) -> runShell(
                    "ssh -l root@%s sed -i /etc/bookstoreapp.conf 's!dbms=__DBMS__!dbms=%s!g'",
                    component.valueOf(WEBSERVER_HOST),
                    component.valueOf(DBSERVER_ENDPOINT)
                )))))
    ).$());
    Attr UNINSTALL = Attribute.create(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.<BookstoreApp.Attr>of(
            attrComponent -> named("Do something for uninstallation", nop())
        ))
    ).$());
  }
}
