package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Operator;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.Utils;

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
  protected int numOracles() {
    return 2;
  }

  @Override
  protected Action createActionForSetUp(int testCaseId, Context context, Fixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Context context, Fixture fixture) {
    return context.sequential(
        Utils.createGroupedAction(
            context,
            true,
            Component::uninstall,
            fixture,
            HTTPD, DBMS, APP, PROXY
        ),
        Utils.createGroupedAction(
            context,
            true,
            Component::install,
            fixture,
            HTTPD, DBMS, APP, PROXY
        ));
  }

  @Override
  protected Action createActionForTest(int testCaseId, int testOracleId, Context $, Fixture fixture) {
    return $.simple(String.format("Issue a request to end point[%s,%s]", testCaseId, testOracleId),
        () -> UtUtils.runShell("ssh -l myuser@%s curl '%s'", "localhost", applicationEndpoint(fixture))
    );
  }

  @Override
  protected Action createActionForTearDown(int testCaseId, Context $, Fixture fixture) {
    return $.named("Collect log files", $.nop());
  }

  @Override
  protected Action createActionForTearDownLastTime(Context $, Fixture fixture) {
    return $.nop();
  }

  @Override
  protected FloorPlan configureFloorPlan(FloorPlan floorPlan) {
    return floorPlan.add(APP, HTTPD, DBMS, PROXY)
        .wire(APP, BookstoreApp.Attr.DBSERVER, DBMS)
        .wire(APP, BookstoreApp.Attr.WEBSERVER, HTTPD)
        .wire(PROXY, Nginx.Attr.UPSTREAM, APP)
        .requireProfile(Utils.toPrintablePredicate(
            () -> "isInstanceOf[BookstoreProfile]",
            p -> p instanceof BookstoreProfile)
        );
  }

  @Override
  protected FixtureConfigurator configureFixture(FixtureConfigurator fixtureConfigurator) {
    fixtureConfigurator.lookUp(PROXY).addOperatorFactory(
        Operator.Factory.of(
            Operator.Type.START,
            c -> $ -> $.named("configuredStart", $.nop())
        )).addOperatorFactory(
        Operator.Factory.of(
            Operator.Type.NUKE,
            c -> $ -> $.named("configuredNuke", $.nop())
        ));
    return fixtureConfigurator;
  }

  @Override
  public String applicationEndpoint(Fixture fixture) {
    return requireNonNull(fixture).lookUp(PROXY).valueOf(Nginx.Attr.ENDPOINT);
  }
}



