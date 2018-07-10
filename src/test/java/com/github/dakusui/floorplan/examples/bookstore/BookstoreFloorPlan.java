package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FixtureConfigurator;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;

import static java.util.Arrays.asList;

public abstract class BookstoreFloorPlan<F extends BookstoreFloorPlan<F>> extends FloorPlan.Base<BookstoreFloorPlan<F>> {
  public final Ref app   = Ref.ref(BookstoreApp.SPEC, "1");
  public final Ref httpd = Ref.ref(Apache.SPEC, "1");
  public final Ref dbms  = Ref.ref(PostgreSQL.SPEC, "1");


  @Override
  public boolean canBeDeployedOn(Profile profile) {
    return super.canBeDeployedOn(profile) && profile instanceof BookstoreProfile;
  }

  public abstract Fixture.Factory fixtureFactory();

  public static class NoProxy extends BookstoreFloorPlan<NoProxy> {
    NoProxy() {
      this.add(httpd).add(dbms).add(app)
          .wire(app, BookstoreApp.Attr.DBSERVER, dbms)
          .wire(app, BookstoreApp.Attr.WEBSERVER, httpd);
    }

    @Override
    public Fixture.Factory fixtureFactory() {
      return new Fixture.Factory() {
        @Override
        public BookstoreFixture create(Policy policy, FixtureConfigurator fixtureConfigurator) {
          return new BookstoreFixture(policy, fixtureConfigurator) {
            @Override
            public String endpoint() {
              return this.lookUp(app).valueOf(BookstoreApp.Attr.ENDPOINT);
            }

            @Override
            public Action install(Context $) {
              return null;
            }
          };
        }
      };
    }
  }

  public static abstract class WithProxy<F extends WithProxy<F>> extends BookstoreFloorPlan<F> {
    public final Ref proxy = Ref.ref(Nginx.SPEC, "1");
  }

  public static class ForSmoke extends WithProxy<ForSmoke> {
    public ForSmoke() {
      this.add(httpd).add(dbms).add(app).add(proxy)
          .wire(app, BookstoreApp.Attr.DBSERVER, dbms)
          .wire(app, BookstoreApp.Attr.WEBSERVER, httpd)
          .wire(proxy, Nginx.Attr.UPSTREAM, app)
      ;
    }

    @Override
    public Fixture.Factory<BookstoreFixture> fixtureFactory() {
      return new Fixture.Factory<BookstoreFixture>() {
        @Override
        public BookstoreFixture create(Policy policy, FixtureConfigurator fixtureConfigurator) {
          return new BookstoreFixture(policy, fixtureConfigurator) {
            @Override
            public String endpoint() {
              return this.lookUp(proxy).valueOf(Nginx.Attr.ENDPOINT);
            }

            @Override
            public Action install(Context $) {
              return null;
            }
          };
        }
      };
    }
  }

  public static class ForAvailability extends WithProxy<ForAvailability> {
    public final Ref app2   = Ref.ref(BookstoreApp.SPEC, "2");
    public final Ref httpd2 = Ref.ref(Apache.SPEC, "2");
    public final Ref app3   = Ref.ref(BookstoreApp.SPEC, "3");
    public final Ref httpd3 = Ref.ref(Apache.SPEC, "3");

    @Override
    public boolean canBeDeployedOn(Profile profile) {
      return super.canBeDeployedOn(profile) &&
          asList(
              BookstoreProfile.Category.PROD,
              BookstoreProfile.Category.DEV
          ).contains(
              BookstoreProfile.class.cast(profile).getCategory()
          );
    }

    @Override
    public Fixture.Factory fixtureFactory() {
      return new Fixture.Factory() {
        @Override
        public BookstoreFixture create(Policy policy, FixtureConfigurator fixtureConfigurator) {
          return new BookstoreFixture(policy, fixtureConfigurator) {
            @Override
            public String endpoint() {
              return this.lookUp(proxy).valueOf(Nginx.Attr.ENDPOINT);
            }

            @Override
            public Action install(Context $) {
              return null;
            }
          };
        }
      };
    }
  }
}
