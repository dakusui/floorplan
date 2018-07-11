package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFixture;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreFloorPlan;
import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.*;

import java.util.List;

import static java.util.Arrays.asList;

public class BookstoreExample {
  @SuppressWarnings("unchecked")
  private static final TestSuiteDescriptor DESCRIPTOR = new TestSuiteDescriptor.Factory.Base<BookstoreFloorPlan.ForSmoke, BookstoreFixture>() {
    @Override
    protected String name() {
      return "example";
    }

    @Override
    protected String nameFor(int i) {
      return String.format("example-test[%02d]", i);
    }

    @Override
    protected int numTests() {
      return 1;
    }

    @Override
    protected BookstoreFloorPlan.ForSmoke buildFloorPlan() {
      return new BookstoreFloorPlan.ForSmoke();
    }

    @Override
    protected Fixture.Factory<BookstoreFixture> createFixtureFactory() {
      return new Fixture.Factory<BookstoreFixture>() {
        @Override
        public BookstoreFixture create(Policy policy, FixtureConfigurator fixtureConfigurator) {
          return new BookstoreFixture(policy, fixtureConfigurator) {
            @Override
            public String applicationEndpoint() {
              return this.lookUp(floorPlan().proxy).valueOf(Nginx.Attr.ENDPOINT);
            }
          };
        }
      };
    }

    @Override
    protected List<ComponentSpec<?>> allKnownComponentSpecs() {
      return asList(Apache.SPEC, PostgreSQL.SPEC, BookstoreApp.SPEC, Nginx.SPEC);
    }

    @Override
    protected Action createActionForSetUp(int i, Context context, BookstoreFixture fixture) {
      return context.nop();
    }

    @Override
    protected Action createActionForSetUpFirstTime(Context context, BookstoreFixture fixture) {
      return Utils.createGroupedAction(
          context,
          true,
          Component::install,
          fixture,
          floorPlan().dbms, floorPlan().httpd, floorPlan().app, floorPlan().proxy
      );
    }

    @Override
    protected Action createActionForTest(int i, Context context, BookstoreFixture fixture) {
      return context.simple("Issue a request to end point",
          () -> UtUtils.runShell("ssh -l myuser@%s curl '%s'", "localhost", fixture.applicationEndpoint())
      );
    }

    @Override
    protected Action createActionForTearDown(int i, Context context, BookstoreFixture fixture) {
      return context.named("Collect log files", context.nop());
    }

    @Override
    protected Action createActionForTearDownLastTime(Context context, BookstoreFixture fixture) {
      return context.nop();
    }
  }.create(new BookstoreProfile());

  @BeforeClass
  public static void beforeAll() {
    System.out.printf("TestSuite:%s[%stests]%n", DESCRIPTOR.getName(), DESCRIPTOR.size());
    Utils.perform(DESCRIPTOR.setUpFirstTime(Utils.newContext()));
  }

  @Before
  public void before() {
    System.out.printf("TestSuite:%s.TestCase(%s)%n", DESCRIPTOR.getName(), DESCRIPTOR.getNameFor(0));
    Utils.perform(DESCRIPTOR.setUp(Utils.newContext(), 0));
  }

  @Test
  public void test() {
    System.out.printf("TestSuite:%s.TestCase(%s)%n", DESCRIPTOR.getName(), DESCRIPTOR.getNameFor(0));
    Utils.perform(DESCRIPTOR.test(Utils.newContext(), 0));
  }

  @After
  public void after() {
    System.out.printf("TestSuite:%s.TestCase(%s)%n", DESCRIPTOR.getName(), DESCRIPTOR.getNameFor(0));
    Utils.perform(DESCRIPTOR.tearDown(Utils.newContext(), 0));
  }

  @AfterClass
  public static void afterAll() {
    System.out.printf("TestSuite:%s%n", DESCRIPTOR.getName());
    Utils.perform(DESCRIPTOR.tearDownLastTime(Utils.newContext()));
  }
}
