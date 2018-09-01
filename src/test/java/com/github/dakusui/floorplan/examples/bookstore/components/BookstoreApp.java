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
import static com.github.dakusui.floorplan.utils.FloorPlanUtils.resolve;

/**
 * A class to define a deployment specification of an internet bookstore
 * application.
 */
public interface BookstoreApp extends Component<BookstoreApp.Attr> {
  ComponentSpec<Attr> SPEC = ComponentSpec.create(BookstoreApp.class);

  interface Attr extends Attribute {
    Attr APPNAME        = SPEC.property(String.class).defaultsTo(immediate("bookstore")).define();
    Attr WEBSERVER      = SPEC.property(Apache.SPEC).required().define();
    Attr WEBSERVER_HOST = SPEC.property(String.class).defaultsTo(attributeValueOf(Apache.Attr.HOSTNAME, referenceTo(WEBSERVER))).define();
    Attr WEBSERVER_PORT = SPEC.property(Integer.class).defaultsTo(attributeValueOf(Apache.Attr.PORTNUMBER, referenceTo(WEBSERVER))).define();
    Attr DBSERVER       = SPEC.property(PostgreSQL.SPEC).required().define();
    @SuppressWarnings("unchecked")
    Attr ADMIN_USER = SPEC.property(String.class).optional().define();
    Attr ADMIN_MODE        = SPEC.property(Boolean.class).defaultsTo(immediate(false)).define();
    Attr DBSERVER_ENDPOINT = SPEC.property(String.class).defaultsTo(
        Resolver.of(
            c -> p -> {
              Configurator<PostgreSQL.Attr> dbServer = p.lookUp(resolve(DBSERVER, c, p));
              return String.format(
                  "jdbc:postgresql://%s:%s/%s",
                  resolve(PostgreSQL.Attr.HOSTNAME, dbServer, p),
                  resolve(PostgreSQL.Attr.PORTNUMBER, dbServer, p),
                  resolve(PostgreSQL.Attr.BOOKSTORE_DATABASE, dbServer, p)
              );
            },
            () -> "An endpoint to access a database server where data of this application is stored."
        )
    ).define();
    Attr ENDPOINT          = SPEC.property(String.class).defaultsTo(
        Resolver.of(
            c -> p -> {
              Configurator<Apache.Attr> webServer = p.floorPlanConfigurator().lookUp(FloorPlanUtils.resolve(WEBSERVER, c, p));
              return String.format(
                  "http://%s:%s/%s",
                  resolve(Apache.Attr.HOSTNAME, webServer, p),
                  resolve(Apache.Attr.PORTNUMBER, webServer, p),
                  resolve(APPNAME, c, p)
              );
            },
            () -> "An endpoint to access this application"
        )
    ).define();
    Attr INSTALL           = SPEC.property(ActionFactory.class).defaultsTo(
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
    ).define();
    Attr UNINSTALL         = SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.<BookstoreApp.Attr>of(
            attrComponent -> named("Do something for uninstallation", nop())
        ))
    ).define();
  }

  default String adminUser() {
    if (this.valueOf(Attr.ADMIN_MODE))
      return this.valueOf(Attr.ADMIN_USER);
    throw new IllegalStateException("Not in admin mode!");
  }
}
