package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.examples.bookstore.BookstoreExample;
import com.github.dakusui.floorplan.tdesc.junit4.StandardTestBase;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner.UseProfileFactory;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner.UseTestSuiteDescriptorFactory;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;
import static org.junit.runner.JUnitCore.runClasses;

public class StandardTestBaseTest extends UtBase {

  @RunWith(FloorPlanRunner.class)
  @UseProfileFactory(BookstoreExample.ProfileFactory.class)
  @UseTestSuiteDescriptorFactory(StandardTestBaseTestWith4Oracles.DescFactory.class)
  public static class StandardTestBaseTestWith4Oracles extends StandardTestBase {
    public StandardTestBaseTestWith4Oracles(String testSuiteName, String testCaseName, Map<String, Function<Context, Named>> testActionFactories) {
      super(testSuiteName, testCaseName, testActionFactories);
    }

    public static class DescFactory extends UtTsDescriptorFactory {
      @Override
      public int numOracles() {
        return 4;
      }
    }
  }

  @RunWith(FloorPlanRunner.class)
  @UseProfileFactory(BookstoreExample.ProfileFactory.class)
  @UseTestSuiteDescriptorFactory(StandardTestBaseTestWith5Oracles.DescFactory.class)
  public static class StandardTestBaseTestWith5Oracles extends StandardTestBase {
    public StandardTestBaseTestWith5Oracles(String testSuiteName, String testCaseName, Map<String, Function<Context, Named>> testActionFactories) {
      super(testSuiteName, testCaseName, testActionFactories);
    }

    public static class DescFactory extends UtTsDescriptorFactory {
      @Override
      public int numOracles() {
        return 5;
      }
    }
  }

  @Test
  public void test4() {
    assertThat(
        runClasses(StandardTestBaseTestWith4Oracles.class),
        allOf(
            asInteger("getRunCount").equalTo(10).$(),
            asInteger("getIgnoreCount").equalTo(0).$(),
            asInteger("getFailureCount").equalTo(0).$()
        )
    );
  }

  @Test
  public void test5() {
    assertThat(
        runClasses(StandardTestBaseTestWith4Oracles.class),
        allOf(
            asInteger("getRunCount").equalTo(10).$(),
            asInteger("getIgnoreCount").equalTo(0).$(),
            asInteger("getFailureCount").equalTo(0).$()
        )
    );
  }
}
