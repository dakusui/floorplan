package com.github.dakusui.floorplan.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.examples.bookstore.BookstoreFloorPlan;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;
import org.junit.Test;

public class BookstoreExample {
  BookstoreFloorPlan.ForSmoke floorPlan = new BookstoreFloorPlan.ForSmoke();

  private Policy buildPolicyForBookStoreSystem(BookstoreFloorPlan floorPlan, Profile profile) {
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

  @Test
  public void scenario() {

  }


}
