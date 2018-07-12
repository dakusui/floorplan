package com.github.dakusui.floorplan.tdesc.junit4;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanParametersRunnerFactory;
import com.github.dakusui.floorplan.tdesc.junit4.runner.FloorPlanRunner;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.AfterClass;
import org.junit.AssumptionViolatedException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.github.dakusui.floorplan.utils.Utils.newContext;
import static java.util.stream.Collectors.toList;

@RunWith(FloorPlanRunner.class)
@UseParametersRunnerFactory(FloorPlanParametersRunnerFactory.class)
@FixMethodOrder(MethodSorters.JVM)
public class TestBase {
  protected final String                                testCaseName;
  protected final Map<String, Function<Context, Named>> testActionFactories;
  protected final LinkedList<String>                    testOracleNames;
  protected final String                                testSuiteName;
  protected final Context                               context = newContext();

  @SuppressWarnings("WeakerAccess")
  protected TestBase(String testSuiteName, Map<String, Function<Context, Named>> testActionFactories, String testCaseName) {
    this.testSuiteName = testSuiteName;
    this.testActionFactories = testActionFactories;
    this.testOracleNames = new LinkedList<>(testActionFactories.keySet());
    this.testCaseName = testCaseName;
  }

  @Parameterized.Parameters(name = "{index}:{0}")
  public static Collection<Object[]> data(TestSuiteDescriptor descriptor) {
    return IntStream.range(0, descriptor.size()).mapToObj(
        i -> new Object[] {
            descriptor.getName(),
            descriptor.getTestCaseNameFor(i),
            new LinkedHashMap<String, Function<Context, Named>>() {{
              IntStream.range(0, descriptor.numTestOracles())
                  .forEach(j -> put(
                      descriptor.getTestOracleNameFor(j),
                      c -> composeTestAction(c, descriptor, i, j)
                  ));
            }}
        }
    ).collect(toList());
  }

  @BeforeClass
  public static void beforeAll(TestSuiteDescriptor descriptor) {
    Utils.performAction(descriptor.setUpFirstTime(newContext()));
  }

  @AfterClass
  public static void afterAll(TestSuiteDescriptor descriptor) {
    Utils.performAction(descriptor.tearDownLastTime(newContext()));
  }

  @SuppressWarnings("WeakerAccess")
  protected static AssumptionViolatedException noTestOracleFor(int oracleId) {
    throw new AssumptionViolatedException(String.format("No test oracle provided for id:%s", oracleId));
  }

  private static Named composeTestAction(Context $, TestSuiteDescriptor tsDesc, int i, int j) {
    return (Named) $.named(tsDesc.getTestOracleNameFor(j),
        $.sequential(
            tsDesc.setUp($, i),
            $.attempt(
                tsDesc.test($, i, j)
            ).recover(
                AssertionError.class, ($$, supplier) -> $$.simple("rethrow", () -> {
                  Throwable t = supplier.get();
                  if (t instanceof AssertionError)
                    throw (AssertionError) t;
                  throw new RuntimeException(String.format("Exception was caught:%s%n", t.getMessage()), t);
                })
            ).ensure(
                $$ -> tsDesc.tearDown($$, i)
            )));
  }

  @SuppressWarnings("WeakerAccess")
  protected void performTest(int oracleId) {
    Utils.performAction(testActionFactories.get(testOracleNames.get(oracleId)).apply(this.context));
  }

  @SuppressWarnings("WeakerAccess")
  protected void performTests(int fromOracleIdInclusive, int toOracleIdExclusive) {
    Utils.performAction(this.context.named(
        this.testCaseName,
        this.context.concurrent(
            IntStream.range(fromOracleIdInclusive, toOracleIdExclusive)
                .mapToObj(oracleId -> testActionFactories.get(testOracleNames.get(oracleId)).apply(context))
                .collect(toList())
        )
    ));
  }
}
