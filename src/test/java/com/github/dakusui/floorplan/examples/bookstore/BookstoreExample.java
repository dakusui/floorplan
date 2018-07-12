package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.tdescs.SmokeTestDesc;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dakusui.floorplan.utils.Checks.requireArgument;
import static com.github.dakusui.floorplan.utils.Utils.newContext;
import static java.util.stream.Collectors.toList;

@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.JVM)
public class BookstoreExample {
  @SuppressWarnings("unchecked")
  private static final TestSuiteDescriptor                   DESCRIPTOR = new SmokeTestDesc().create(new BookstoreProfile());
  private final        String                                testCaseName;
  private final        Map<String, Function<Context, Named>> testActionFactories;
  private final        LinkedList<String>                    testOracleNames;
  private final        Context                               context    = newContext();

  public BookstoreExample(String testCaseName, Map<String, Function<Context, Named>> testActionFactories) {
    this.testCaseName = testCaseName;
    this.testActionFactories = testActionFactories;
    this.testOracleNames = new LinkedList<>(testActionFactories.keySet());
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return IntStream.range(0, DESCRIPTOR.size()).mapToObj(
        i -> new LinkedList<Object>() {{
          add(DESCRIPTOR.getTestCaseNameFor(i));
          add(new LinkedHashMap<String, Function<Context, Named>>() {
                {
                  IntStream.range(0, DESCRIPTOR.numTestOracles()).forEach(
                      j -> put(
                          DESCRIPTOR.getTestOracleNameFor(j),
                          context -> composeTestAction(context, DESCRIPTOR, i, j)
                      ));
                }
              }
          );
        }}.toArray()
    ).collect(toList());
  }

  @BeforeClass
  public static void beforeAll() {
    System.out.printf("TestSuite:%s[%stests]%n", DESCRIPTOR.getName(), DESCRIPTOR.size());
    Utils.performAction(DESCRIPTOR.setUpFirstTime(newContext()));
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
  public void executeRemainingTests() {
    ifPresentPerformTestsFrom(3);
  }

  @AfterClass
  public static void afterAll() {
    System.out.printf("TestSuite:%s%n", DESCRIPTOR.getName());
    Utils.performAction(DESCRIPTOR.tearDownLastTime(newContext()));
  }

  private void performTestIfPresent(int oracleId) {
    if (oracleId < this.testActionFactories.size())
      performTest(oracleId);
    else
      throw noTestOracleFor(oracleId);
  }

  protected void performTest(int oracleId) {
    Utils.performAction(testActionFactories.get(testOracleNames.get(oracleId)).apply(this.context));
  }

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

  protected void ifPresentPerformTestsFrom(int firstOracleId) {
    if (firstOracleId >= this.testActionFactories.size())
      throw noTestOracleFor(firstOracleId);
    if (firstOracleId == this.testActionFactories.size() - 1)
      performTest(firstOracleId);
    else
      performTests(firstOracleId, this.testOracleNames.size());
  }

  private static AssumptionViolatedException noTestOracleFor(int oracleId) {
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
}
