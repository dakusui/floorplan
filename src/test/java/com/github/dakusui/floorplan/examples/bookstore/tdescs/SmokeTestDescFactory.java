package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FloorPlanDescriptor;
import com.github.dakusui.floorplan.examples.bookstore.components.*;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.InternalUtils;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public class SmokeTestDescFactory extends BasicTestDescFactory {
  @Override
  protected String name() {
    return "Smoke";
  }

  @Override
  protected int numTests() {
    return 2;
  }

  @Override
  protected int numTestOracles() {
    return 2;
  }

  @Override
  protected Action createActionForSetUp(int testCaseId, FloorPlan floorPlan) {
    return nop();
  }

  private <A extends Attribute> Action toAction(Component<A> component, A actionFactoryAttribute) {
    return component.<ActionFactory<A>>valueOf(actionFactoryAttribute).create(component);
  }


  @Override
  protected Action createActionForSetUpFirstTime(FloorPlan floorPlan) {
    return sequential(
        parallel(
            toAction(floorPlan.lookUp(HTTPD), Apache.Attr.UNINSTALL),
            toAction(floorPlan.lookUp(DBMS), PostgreSQL.Attr.UNINSTALL),
            toAction(floorPlan.lookUp(APP), BookstoreApp.Attr.UNINSTALL),
            toAction(floorPlan.lookUp(PROXY), Nginx.Attr.UNINSTALL)
        ),
        parallel(
            toAction(floorPlan.lookUp(HTTPD), Apache.Attr.INSTALL),
            toAction(floorPlan.lookUp(DBMS), PostgreSQL.Attr.INSTALL),
            toAction(floorPlan.lookUp(APP), BookstoreApp.Attr.INSTALL),
            toAction(floorPlan.lookUp(PROXY), Nginx.Attr.INSTALL)
        )
    );
  }

  @Override
  protected Action createActionForTest(int testCaseId, int testOracleId, FloorPlan floorPlan) {
    return simple(String.format("Issue a request to end point[%s,%s]", testCaseId, testOracleId),
        (c) -> UtUtils.runShell("ssh -l myuser@%s curl '%s'", "localhost", applicationEndpoint(floorPlan))
    );
  }

  @Override
  protected Action createActionForTearDown(int testCaseId, FloorPlan floorPlan) {
    return named("Collect log files", nop());
  }

  @Override
  protected Action createActionForTearDownLastTime(FloorPlan floorPlan) {
    return nop();
  }

  @Override
  protected FloorPlanDescriptor buildFloorPlanDescriptor(FloorPlanDescriptor.Builder floorPlanDescriptorBuilder) {
    return floorPlanDescriptorBuilder
        .wire(APP, BookstoreApp.Attr.DBSERVER, DBMS)
        .wire(APP, BookstoreApp.Attr.WEBSERVER, HTTPD)
        .wire(PROXY, Nginx.Attr.UPSTREAM, APP)
        .configure(
            PROXY, Nginx.Attr.START, immediate(ActionFactory.of(
                c -> named("configuredStart", nop())
            )))
        .configure(
            PROXY, Nginx.Attr.NUKE, immediate(ActionFactory.of(
                c -> named("configuredNuke", nop())
            )))
        .build();
  }

  @Override
  protected Predicate<Profile> profileRequirement() {
    return InternalUtils.toPrintablePredicate(
        () -> "isInstanceOf[BookstoreProfile]",
        profile -> profile instanceof BookstoreProfile
    );
  }

  @Override
  public String applicationEndpoint(FloorPlan floorPlan) {
    return requireNonNull(floorPlan).lookUp(PROXY).valueOf(Nginx.Attr.ENDPOINT);
  }
}
