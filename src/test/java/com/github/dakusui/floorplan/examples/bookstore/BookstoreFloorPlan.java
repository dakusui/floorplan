package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Profile;

import static java.util.Arrays.asList;

public abstract class BookstoreFloorPlan extends FloorPlan {
  public final Ref app   = Ref.ref(BookstoreApp.SPEC, "1");
  public final Ref httpd = Ref.ref(Apache.SPEC, "1");
  public final Ref dbms  = Ref.ref(PostgreSQL.SPEC, "1");


  public abstract String endpoint(Fixture fixture);

  @Override
  public boolean canBeDeployedOn(Profile profile) {
    return super.canBeDeployedOn(profile) && profile instanceof BookstoreFloorPlan;
  }

  public static class NoProxy extends BookstoreFloorPlan {
    NoProxy() {
      this.add(httpd).add(dbms).add(app)
          .wire(app, BookstoreApp.Attr.DBSERVER, dbms)
          .wire(app, BookstoreApp.Attr.WEBSERVER, httpd);
    }

    @Override
    public String endpoint(Fixture fixture) {
      return fixture.lookUp(app).valueOf(BookstoreApp.Attr.ENDPOINT);
    }
  }

  public static abstract class WithProxy extends BookstoreFloorPlan {
    public final Ref proxy = Ref.ref(Nginx.SPEC, "1");


    @Override
    public String endpoint(Fixture fixture) {
      return fixture.lookUp(proxy).valueOf(Nginx.Attr.ENDPOINT);
    }
  }

  public static class ForSmoke extends WithProxy {
    public ForSmoke() {
      this.add(httpd).add(dbms).add(app).add(proxy)
          .wire(app, BookstoreApp.Attr.DBSERVER, dbms)
          .wire(app, BookstoreApp.Attr.WEBSERVER, httpd)
          .wire(proxy, Nginx.Attr.UPSTREAM, app)
      ;
    }
  }

  public static class ForAvailability extends WithProxy {
    public final Ref app2   = Ref.ref(BookstoreApp.SPEC, "2");
    public final Ref httpd2 = Ref.ref(Apache.SPEC, "2");
    public final Ref app3   = Ref.ref(BookstoreApp.SPEC, "3");
    public final Ref httpd3 = Ref.ref(Apache.SPEC, "3");

    @Override
    public String endpoint(Fixture fixture) {
      return fixture.lookUp(proxy).valueOf(Nginx.Attr.ENDPOINT);
    }

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
  }
}
