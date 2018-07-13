package com.github.dakusui.floorplan.examples.bookstore.floorplan;

import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.policy.Policy;

public abstract class BookstoreFixture extends Fixture.Impl {

  protected BookstoreFixture(Policy policy, FixtureConfigurator fixtureConfigurator) {
    super(policy, fixtureConfigurator);
  }

  public abstract String applicationEndpoint();
}
