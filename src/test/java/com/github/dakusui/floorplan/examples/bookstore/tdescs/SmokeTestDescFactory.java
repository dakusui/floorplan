package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
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
  protected Action createActionForSetUp(int testCaseId, Fixture fixture) {
    return nop();
  }

  private <A extends Attribute> Action toAction(Component<A> component, A actionFactoryAttribute) {
    return component.<ActionFactory<A>>valueOf(actionFactoryAttribute).create(component);
  }


  @Override
  protected Action createActionForSetUpFirstTime(Fixture fixture) {
    return sequential(
        parallel(
            toAction(fixture.lookUp(HTTPD), Apache.Attr.UNINSTALL),
            toAction(fixture.lookUp(DBMS), PostgreSQL.Attr.UNINSTALL),
            toAction(fixture.lookUp(APP), BookstoreApp.Attr.UNINSTALL),
            toAction(fixture.lookUp(PROXY), Nginx.Attr.UNINSTALL)
        ),
        parallel(
            toAction(fixture.lookUp(HTTPD), Apache.Attr.INSTALL),
            toAction(fixture.lookUp(DBMS), PostgreSQL.Attr.INSTALL),
            toAction(fixture.lookUp(APP), BookstoreApp.Attr.INSTALL),
            toAction(fixture.lookUp(PROXY), Nginx.Attr.INSTALL)
        )
    );
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
  public String applicationEndpoint(Fixture fixture) {
    return requireNonNull(fixture).lookUp(PROXY).valueOf(Nginx.Attr.ENDPOINT);
  }
}
