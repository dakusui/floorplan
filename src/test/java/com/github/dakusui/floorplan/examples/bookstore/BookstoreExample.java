package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dakusui.floorplan.utils.Utils.newContext;
import static java.util.stream.Collectors.toList;

@RunWith(FloorPlanRunner.class)
@Parameterized.UseParametersRunnerFactory(FloorPlanParametersRunnerFactory.class)
public class BookstoreExample {
  @SuppressWarnings("unchecked")
  private final String      testCaseName;
  private final List<Named> testActions;
  private final String      testSuiteName;

  public BookstoreExample(String testSuiteName, String testCaseName, List<Named> testActions) {
    this.testSuiteName = testSuiteName;
    this.testCaseName = testCaseName;
    this.testActions = testActions;
  }

  @Parameterized.Parameters(name = "{index}:{0}")
  public static Collection<Object[]> data(TestSuiteDescriptor descriptor) {
    return IntStream.range(0, descriptor.size()).mapToObj(
        i -> new Object[] {
            descriptor.getName(),
            descriptor.getTestCaseNameFor(i),
            IntStream.range(0, descriptor.numTestOracles())
                .mapToObj(j -> composeTestAction(newContext(), descriptor, i, j))
                .collect(Collectors.toList())
        }
    ).collect(toList());
  }

  @BeforeClass
  public static void beforeAll(TestSuiteDescriptor descriptor) {
    Utils.performAction(descriptor.setUpFirstTime(newContext()));
  }

  @Test
  public void execute0() {
    Utils.performAction(testActions.get(0));
  }


  @AfterClass
  public static void afterAll(TestSuiteDescriptor descriptor) {
    Utils.performAction(descriptor.tearDownLastTime(newContext()));
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
}
