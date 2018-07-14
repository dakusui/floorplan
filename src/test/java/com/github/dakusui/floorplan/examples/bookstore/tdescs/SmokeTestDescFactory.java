package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Operator;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFixture;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFloorPlan;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.Utils;

import java.util.List;

import static java.util.Arrays.asList;

public class SmokeTestDescFactory extends TestSuiteDescriptor.Factory.Base<BookstoreFloorPlan.ForSmoke, BookstoreFixture> {
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

  @Override
  protected BookstoreFloorPlan.ForSmoke buildFloorPlan() {
    return new BookstoreFloorPlan.ForSmoke();
  }

  @Override
  protected Fixture.Factory createFixtureFactory() {
    return new Fixture.Factory() {
      @Override
      public BookstoreFixture create(Policy policy, FixtureConfigurator fixtureConfigurator) {
        fixtureConfigurator.lookUp(floorPlan().proxy)
            .addOperator(
                Operator.Factory.of(
                    Operator.Type.NUKE,
                    c -> $ -> $.named("configuredStart", $.nop())
                ).apply((ComponentSpec<Attribute>) floorPlan().proxy.spec()))
            .addOperator(
                Operator.Factory.of(
                    Operator.Type.NUKE,
                    c -> $ -> $.named("configuredStart", $.nop())
                ).apply((ComponentSpec<Attribute>) floorPlan().proxy.spec()))
            ;
        return new BookstoreFixture(policy, fixtureConfigurator) {
          @Override
          public String applicationEndpoint() {
            return this.lookUp(floorPlan().proxy).valueOf(Nginx.Attr.ENDPOINT);
          }
        };
      }
    };
  }

  @Override
  protected List<ComponentSpec<?>> allKnownComponentSpecs() {
    return asList(Apache.SPEC, PostgreSQL.SPEC, BookstoreApp.SPEC, Nginx.SPEC);
  }

  @Override
  protected Action createActionForSetUp(int i, Context context, BookstoreFixture fixture) {
    return context.nop();
  }

  @Override
  protected Action createActionForSetUpFirstTime(Context context, BookstoreFixture fixture) {
    return context.sequential(
        Utils.createGroupedAction(
            context,
            true,
            Component::uninstall,
            fixture,
            floorPlan().dbms, floorPlan().httpd, floorPlan().app, floorPlan().proxy
        ),
        Utils.createGroupedAction(
            context,
            true,
            Component::install,
            fixture,
            floorPlan().dbms, floorPlan().httpd, floorPlan().app, floorPlan().proxy
        ));
  }

  @Override
  protected Action createActionForTest(int i, int j, Context $, BookstoreFixture fixture) {
    return $.simple("Issue a request to end point",
        () -> UtUtils.runShell("ssh -l myuser@%s curl '%s'", "localhost", fixture.applicationEndpoint())
    );
  }

  @Override
  protected Action createActionForTearDown(int i, Context $, BookstoreFixture fixture) {
    return $.named("Collect log files", $.nop());
  }

  @Override
  protected Action createActionForTearDownLastTime(Context $, BookstoreFixture fixture) {
    return $.nop();
  }
}


