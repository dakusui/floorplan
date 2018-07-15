package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Operator;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFixture.*;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.Utils;

import java.util.List;

import static com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFixture.*;
import static com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFixture.Basic.PROXY;
import static java.util.Arrays.asList;

public class SmokeTestDescFactory extends TestSuiteDescriptor.Factory.Base<Basic> {
  @Override
  protected String name() {
    return "example";
  }

  @Override
  protected String testCaseNameFor(int i) {
    return String.format("case[%02d]", i);
  }

  @Override
  protected String testOracleNameFor(int j) {
    return String.format("oracle[%02d]", j);
  }

  @Override
  protected int numTests() {
    return 2;
  }

  @Override
  protected int numOracles() {
    return 2;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Fixture.Factory createFixtureFactory() {
    return (policy, fixtureConfigurator) -> {
      fixtureConfigurator.lookUp(PROXY)
          .addOperator(
              Operator.Factory.of(
                  Operator.Type.START,
                  c -> $ -> $.named("configuredStart", $.nop())
              ).apply((ComponentSpec<Attribute>) PROXY.spec()))
          .addOperator(
              Operator.Factory.of(
                  Operator.Type.NUKE,
                  c -> $ -> $.named("configuredNuke", $.nop())
              ).apply((ComponentSpec<Attribute>) PROXY.spec()))
      ;
      return new Basic(policy, fixtureConfigurator);
    };
  }

  @Override
  protected List<ComponentSpec<?>> allKnownComponentSpecs() {
    return asList(Apache.SPEC, PostgreSQL.SPEC, BookstoreApp.SPEC, Nginx.SPEC);
  }

  @Override
  protected Action createActionForSetUp(int i, Context context, Basic fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Context context, Basic fixture) {
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
  protected Action createActionForTest(int i, int j, Context $, Basic fixture) {
    return $.simple("Issue a request to end point",
        () -> UtUtils.runShell("ssh -l myuser@%s curl '%s'", "localhost", fixture.applicationEndpoint())
    );
  }

  @Override
  protected Action createActionForTearDown(int i, Context $, Basic fixture) {
    return $.named("Collect log files", $.nop());
  }

  @Override
  protected Action createActionForTearDownLastTime(Context $, Basic fixture) {
    return $.nop();
  }

  @Override
  protected FloorPlan configureFloorPlan(FloorPlan floorPlan) {
    return floorPlan.add(Basic.APP, Basic.HTTPD, Basic.DBMS, Basic.PROXY)
        .wire(APP, BookstoreApp.Attr.DBSERVER, DBMS)
        .wire(APP, BookstoreApp.Attr.WEBSERVER, HTTPD)
        .wire(PROXY, Nginx.Attr.UPSTREAM, HTTPD)
        .requires(p -> p instanceof BookstoreProfile);
  }
}


