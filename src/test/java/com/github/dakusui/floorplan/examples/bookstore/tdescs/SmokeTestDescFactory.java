package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Operator;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.tdescs.BasicTestDescFactory;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;
import com.github.dakusui.floorplan.utils.InternalUtils;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.floorplan.component.Operator.Type.NUKE;
import static com.github.dakusui.floorplan.component.Operator.Type.START;
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
  protected Action createActionForSetUp(int testCaseId, Fixture fixture) {
    return nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Fixture fixture) {
    return sequential(
        FloorPlanUtils.createGroupedAction(
            true,
            Component::uninstall,
            fixture,
            HTTPD, DBMS, APP, PROXY
        ),
        FloorPlanUtils.createGroupedAction(
            true,
            Component::install,
            fixture,
            HTTPD, DBMS, APP, PROXY
        ));
  }

  @Override
  protected Action createActionForTest(int testCaseId, int testOracleId, Fixture fixture) {
    return simple(String.format("Issue a request to end point[%s,%s]", testCaseId, testOracleId),
        (c) -> UtUtils.runShell("ssh -l myuser@%s curl '%s'", "localhost", applicationEndpoint(fixture))
    );
  }

  @Override
  protected Action createActionForTearDown(int testCaseId, Fixture fixture) {
    return named("Collect log files", nop());
  }

  @Override
  protected Action createActionForTearDownLastTime(Fixture fixture) {
    return nop();
  }

  @Override
  protected FixtureDescriptor buildFixtureDescriptor(FixtureDescriptor.Builder builder) {
    return builder
        .wire(APP, BookstoreApp.Attr.DBSERVER, DBMS)
        .wire(APP, BookstoreApp.Attr.WEBSERVER, HTTPD)
        .wire(PROXY, Nginx.Attr.UPSTREAM, APP)
        .addOperatorFactory(
            PROXY,
            Operator.Factory.of(
                START,
                c -> named("configuredStart", nop())))
        .addOperatorFactory(
            PROXY,
            Operator.Factory.of(
                NUKE,
                c -> named("configuredNuke", nop())
            ))
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
  public String applicationEndpoint(Fixture fixture) {
    return requireNonNull(fixture).lookUp(PROXY).valueOf(Nginx.Attr.ENDPOINT);
  }
}
