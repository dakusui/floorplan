package com.github.dakusui.floorplan.tdesc.junit4;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;

public class StandardTestBase extends TestBase {
  public StandardTestBase(String testSuiteName, Map<String, Function<Context, Named>> testActionFactories, String testCaseName) {
    super(testSuiteName, testActionFactories, testCaseName);
  }

  @Test
  public void executeTestAt0() {
    performTestIfPresent(0);
  }

  @Test
  public void executeTestAt1() {
    performTestIfPresent(1);
  }

  @Test
  public void executeTestAt2() {
    performTestIfPresent(2);
  }

  @Test
  public void executeTestAt3() {
    performTestIfPresent(3);
  }

  @Test
  public void executeRemainingTests() {
    ifPresentPerformTestsFrom(4);
  }

  private void performTestIfPresent(int oracleId) {
    if (oracleId < this.testActionFactories.size())
      performTest(oracleId);
    else
      throw noTestOracleFor(oracleId);
  }

  @SuppressWarnings("SameParameterValue")
  private void ifPresentPerformTestsFrom(int firstOracleId) {
    if (firstOracleId >= this.testActionFactories.size())
      throw noTestOracleFor(firstOracleId);
    if (firstOracleId == this.testActionFactories.size() - 1)
      performTest(firstOracleId);
    else
      performTests(firstOracleId, this.testOracleNames.size());
  }
}
