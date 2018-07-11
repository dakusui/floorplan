package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.tdescs.SmokeTestDesc;
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

@RunWith(Parameterized.class)
public class BookstoreExample {
  @SuppressWarnings("unchecked")
  private static final TestSuiteDescriptor DESCRIPTOR = new SmokeTestDesc().create(new BookstoreProfile());
  private final        String              testCaseName;
  private final        List<Named>         testActions;

  public BookstoreExample(String testCaseName, List<Named> testActions) {
    this.testCaseName = testCaseName;
    this.testActions = testActions;
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return IntStream.range(0, DESCRIPTOR.size()).mapToObj(
        i -> new Object[] {
            DESCRIPTOR.getTestCaseNameFor(i),
            IntStream.range(0, DESCRIPTOR.numTestOracles())
                .mapToObj(j -> composeTestAction(newContext(), DESCRIPTOR, i, j))
                .collect(Collectors.toList())
        }
    ).collect(toList());
  }

  @BeforeClass
  public static void beforeAll() {
    System.out.printf("TestSuite:%s[%stests]%n", DESCRIPTOR.getName(), DESCRIPTOR.size());
    Utils.performAction(DESCRIPTOR.setUpFirstTime(newContext()));
  }

  @Test
  public void print0() {
    System.out.printf(
        "TestSuite(%s).TestCase(%s).TestOracle(%s)%n",
        DESCRIPTOR.getName(),
        this.testCaseName,
        DESCRIPTOR.getTestOracleNameFor(0)
    );
    Utils.printAction(testActions.get(0));
  }

  @Test
  public void execute0() {
    System.out.printf(
        "TestSuite(%s).TestCase(%s).TestOracle(%s)%n",
        DESCRIPTOR.getName(),
        this.testCaseName,
        DESCRIPTOR.getTestOracleNameFor(0)
    );
    Utils.performAction(testActions.get(0));
  }


  @AfterClass
  public static void afterAll() {
    System.out.printf("TestSuite:%s%n", DESCRIPTOR.getName());
    Utils.performAction(DESCRIPTOR.tearDownLastTime(newContext()));
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
