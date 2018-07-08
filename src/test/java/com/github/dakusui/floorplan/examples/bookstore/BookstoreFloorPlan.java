package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;

public abstract class BookstoreFloorPlan extends FloorPlan {
  public final Ref httpd = Ref.ref(Apache.SPEC, "1");
  public final Ref dbms  = Ref.ref(PostgreSQL.SPEC, "1");
  public final Ref app   = Ref.ref(BookstoreApp.SPEC, "1");

  public static class ForSmoke extends BookstoreFloorPlan {
    ForSmoke() {
      this.add(httpd).add(dbms).add(app)
          .wire(app, dbms, BookstoreApp.Attr.DBSERVER)
          .wire(app, httpd, BookstoreApp.Attr.WEBSERVER);
    }
  }
}
