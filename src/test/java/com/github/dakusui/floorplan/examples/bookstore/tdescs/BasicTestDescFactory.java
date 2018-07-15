package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;
import com.github.dakusui.floorplan.examples.bookstore.components.Nginx;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;

import java.util.List;

import static java.util.Arrays.asList;

public abstract class BasicTestDescFactory extends TestSuiteDescriptor.Factory.Base {
  static final Ref APP   = Ref.ref(BookstoreApp.SPEC, "1");
  static final Ref HTTPD = Ref.ref(Apache.SPEC, "1");
  static final Ref DBMS  = Ref.ref(PostgreSQL.SPEC, "1");
  static final Ref PROXY = Ref.ref(Nginx.SPEC, "1");

  public abstract String applicationEndpoint(Fixture fixture);


  @Override
  protected String testCaseNameFor(int testCaseId) {
    return String.format("case[%02d]", testCaseId);
  }

  @Override
  protected String testOracleNameFor(int testOracleId) {
    return String.format("oracle[%02d]", testOracleId);
  }

  @Override
  protected List<ComponentSpec<?>> allKnownComponentSpecs() {
    return asList(Apache.SPEC, PostgreSQL.SPEC, BookstoreApp.SPEC, Nginx.SPEC);
  }
}
