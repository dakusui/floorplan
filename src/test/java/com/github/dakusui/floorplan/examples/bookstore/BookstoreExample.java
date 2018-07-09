package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FixtureConfigurator;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;
import org.junit.Test;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;

public class BookstoreExample {
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
        setUpAction(topLevelContext, fixture, dbms, httpd, app)
    ).build().performAndReport();

    ////
    // Test

    ////
    // Tear down
  }

  private Action setUpAction(Context context, Fixture fixture, Ref... refs) {
    return context.sequential(
        Arrays.stream(
            refs
        ).map(
            fixture::lookUp
        ).map(
            Component::install
        ).map(
            actionCreator -> actionCreator.apply(context)
        ).collect(
            toList()
        ).toArray(
            new Action[refs.length]
        )
    );
  }

  private FixtureConfigurator configure(FixtureConfigurator fixtureConfigurator) {
    return fixtureConfigurator;
  }

  private Policy buildPolicy(FloorPlan floorPlan, Profile profile) {
    return new Policy.Builder()
        .addComponentSpec(Apache.SPEC)
        .addComponentSpec(PostgreSQL.SPEC)
        .addComponentSpec(BookstoreApp.SPEC)
        .addComponentSpec(Nginx.SPEC)
        .setFloorPlan(floorPlan)
        .setProfile(profile)
        .build();
  }

  private FloorPlan buildFloorPlan() {
    return new FloorPlan()
        .add(httpd).add(dbms).add(app)
        .wire(app, BookstoreApp.Attr.DBSERVER, dbms)
        .wire(app, BookstoreApp.Attr.WEBSERVER, httpd);
  }
}
