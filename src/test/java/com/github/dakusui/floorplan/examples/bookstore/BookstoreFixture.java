package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FixtureConfigurator;
import com.github.dakusui.floorplan.policy.Policy;

public abstract class BookstoreFixture extends Fixture.Base {

  protected BookstoreFixture(Policy policy, FixtureConfigurator fixtureConfigurator) {
    super(policy, fixtureConfigurator);
  }

  public abstract String applicationEndpoint();
}
