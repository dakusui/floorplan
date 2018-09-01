package com.github.dakusui.floorplan.ut.components;

import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FloorPlanDescriptor;
import com.github.dakusui.floorplan.examples.bookstore.BookstoreExample;
import com.github.dakusui.floorplan.examples.bookstore.components.*;
import com.github.dakusui.floorplan.exception.MissingValueException;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.floorplan.component.Ref.ref;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;

/**
 * A test for Issue #43.
 *
 * @see <a href="https://github.com/dakusui/floorplan/issues/43">issue-43</a>
 */
public class OptionalAttributeTest extends UtBase {
  private Ref rApp   = ref(BookstoreApp.SPEC, "1");
  private Ref rProxy = ref(Nginx.SPEC, "1");
  private Ref rHttpd = ref(Apache.SPEC, "1");
  private Ref rDbms  = ref(PostgreSQL.SPEC, "1");

  @Test(expected = IllegalStateException.class)
  public void givenNonAdminMode$whenTryAdminUser$thenIllegalState() {
    FloorPlan floorPlan = FloorPlanUtils.buildFloorPlan(floorplanDescriptorBuilder().build());

    BookstoreApp app = floorPlan.lookUp(rApp);

    System.out.println(app.adminUser());
  }

  @Test(expected = MissingValueException.class)
  public void givenAdminModeWithoutAdminUserConfigured$whenTryAdminUser$thenMissingValue() throws Throwable {
    FloorPlan floorPlan = FloorPlanUtils.buildFloorPlan(floorplanDescriptorBuilder()
        .configure(rApp, BookstoreApp.Attr.ADMIN_MODE, immediate(true))
        .build()
    );

    BookstoreApp app = floorPlan.lookUp(rApp);

    try {
      app.adminUser();
    } catch (Throwable t) {
      throw UtUtils.rootCause(t);
    }
  }

  @Test
  public void givenAdminModeWithAdminUserConfigured$whenTryAdminUser$thenIamAdmin() {
    FloorPlan floorPlan = FloorPlanUtils.buildFloorPlan(floorplanDescriptorBuilder()
        .configure(rApp, BookstoreApp.Attr.ADMIN_MODE, immediate(true))
        .configure(rApp, BookstoreApp.Attr.ADMIN_USER, immediate("iAmAdmin"))
        .build()
    );

    BookstoreApp app = floorPlan.lookUp(rApp);

    assertThat(
        app,
        asString("adminUser").equalTo("iAmAdmin").$()
    );
  }

  private FloorPlanDescriptor.Builder floorplanDescriptorBuilder() {
    return new FloorPlanDescriptor.Builder(new BookstoreExample.ProfileFactory().create()).wire(rApp, BookstoreApp.Attr.DBSERVER, rDbms)
        .wire(rApp, BookstoreApp.Attr.WEBSERVER, rHttpd)
        .wire(rProxy, Nginx.Attr.UPSTREAM, rApp)
        .configure(
            rProxy, Nginx.Attr.START, immediate(ActionFactory.of(
                c -> named("configuredStart", nop())
            )))
        .configure(
            rProxy, Nginx.Attr.NUKE, immediate(ActionFactory.of(
                c -> named("configuredNuke", nop())
            )));
  }
}
