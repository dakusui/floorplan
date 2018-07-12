package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.tdescs.SmokeTestDescFactory;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.tdesc.junit4.StandardTestBase;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner.UseProfileFactory;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner.UseTestSuiteDescriptorFactor;

import java.util.Map;
import java.util.function.Function;

@UseTestSuiteDescriptorFactor(SmokeTestDescFactory.class)
@UseProfileFactory(BookstoreExample.ProfileFactory.class)
public class BookstoreExample extends StandardTestBase {
  public static class ProfileFactory implements Profile.Factory<BookstoreProfile> {
    @Override
    public BookstoreProfile create() {
      return new BookstoreProfile();
    }
  }

  public BookstoreExample(String testSuiteName, String testCaseName, Map<String, Function<Context, Named>> testActionFactories) {
    super(testSuiteName, testActionFactories, testCaseName);
    System.out.printf("Hello, I am executing a testcase:%s in a test suite:%s. Defined oracles are following.%n",
        this.testCaseName,
        this.testSuiteName
    );
    this.testOracleNames.forEach(k -> System.out.printf("  %s:%s%n", k, this.testActionFactories.get(k)));
  }
}
