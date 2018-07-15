package com.github.dakusui.floorplan.examples.bookstore.floorplan;

import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Policy;

public abstract class BookstoreFixture extends Fixture.Base {
  public static final Ref APP   = Ref.ref(BookstoreApp.SPEC, "1");
  public static final Ref HTTPD = Ref.ref(Apache.SPEC, "1");
  public static final Ref DBMS  = Ref.ref(PostgreSQL.SPEC, "1");

  BookstoreFixture(Policy policy, FixtureConfigurator fixtureConfigurator) {
    super(policy, fixtureConfigurator);
  }

  public abstract String applicationEndpoint();

  public static class Basic extends BookstoreFixture {
    public static final Ref PROXY = Ref.ref(Nginx.SPEC, "1");

    public Basic(Policy policy, FixtureConfigurator fixtureConfigurator) {
      super(policy, fixtureConfigurator);
    }

    @Override
    public String applicationEndpoint() {
      return this.lookUp(PROXY).valueOf(Nginx.Attr.ENDPOINT);
    }
  }

  public static class ForAvailability extends BookstoreFixture.Basic {
    public static final Ref APP_2   = Ref.ref(BookstoreApp.SPEC, "2");
    public static final Ref HTTPD_2 = Ref.ref(Apache.SPEC, "2");
    public static final Ref APP_3   = Ref.ref(BookstoreApp.SPEC, "3");
    public static final Ref HTTPD_3 = Ref.ref(Apache.SPEC, "3");

    public ForAvailability(Policy policy, FixtureConfigurator fixtureConfigurator) {
      super(policy, fixtureConfigurator);
    }
  }
}
