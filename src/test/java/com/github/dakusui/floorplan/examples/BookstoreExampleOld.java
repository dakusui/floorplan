package com.github.dakusui.floorplan.examples;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FixtureConfigurator;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.BookstoreFloorPlan;
import com.github.dakusui.floorplan.examples.bookstore.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.Test;

public class BookstoreExampleOld {
  private Ref     httpd           = Ref.ref(Apache.SPEC, "1");
  private Ref     dbms            = Ref.ref(PostgreSQL.SPEC, "1");
  private Ref     app             = Ref.ref(BookstoreApp.SPEC, "1");
  private Context topLevelContext = new Context.Impl();

  @Test
  public void example() {
    ////
    // Floor plan design
    Policy policy = buildPolicy(
        buildFloorPlan(),
        new BookstoreProfile()
    );

    ////
    // Fixture configuration
    FixtureConfigurator fixtureConfigurator = configure(policy.fixtureConfigurator());

    ////
    // Fixture is frozen
    Fixture fixture = fixtureConfigurator.build();

    ////
    // Set up
    new ReportingActionPerformer.Builder(
        Utils.createGroupedAction(
            topLevelContext,
            true,
            Component::install,
            fixture, dbms, httpd, app
        )
    ).build().performAndReport();

    ////
    // Test

    ////
    // Tear down
  }

  private FixtureConfigurator configure(FixtureConfigurator fixtureConfigurator) {
    return fixtureConfigurator;
  }

  private Policy buildPolicy(BookstoreFloorPlan floorPlan, Profile profile) {
    return new Policy.Builder()
        .addComponentSpec(Apache.SPEC)
        .addComponentSpec(PostgreSQL.SPEC)
        .addComponentSpec(BookstoreApp.SPEC)
        .addComponentSpec(Nginx.SPEC)
        .setFloorPlan(floorPlan)
        .setProfile(profile)
        .setFixtureFactory(floorPlan.fixtureFactory())
        .build();
  }

  private BookstoreFloorPlan buildFloorPlan() {
    return new BookstoreFloorPlan.ForSmoke().add(httpd).add(dbms).add(app)
        .wire(app, BookstoreApp.Attr.DBSERVER, dbms)
        .wire(app, BookstoreApp.Attr.WEBSERVER, httpd);

  }
}
