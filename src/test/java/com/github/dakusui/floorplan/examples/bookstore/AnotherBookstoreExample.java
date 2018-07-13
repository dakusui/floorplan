package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.examples.bookstore.tdescs.SmokeTestDescFactory;
import com.github.dakusui.floorplan.tdesc.junit4.TestBase;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;

@FloorPlanRunner.UseTestSuiteDescriptorFactory(SmokeTestDescFactory.class)
@FloorPlanRunner.UseProfileFactory(BookstoreExample.ProfileFactory.class)
public class AnotherBookstoreExample extends TestBase {
  public AnotherBookstoreExample(String testSuiteName, String testCaseName, Map<String, Function<Context, Named>> testActionFactories) {
    super(testSuiteName, testCaseName, testActionFactories);
  }

  @Test
  public void executeAllTests() {
    performTests(0, this.testOracleNames.size());
  }

  @Test
  public void executeAllButFirstTests() {
    performTests(1, this.testOracleNames.size());
  }
}
