package com.github.dakusui.floorplan.examples.bookstore.tdescs;

import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.BookstoreApp;

public abstract class AvailabilityTestDescFactory extends BasicTestDescFactory {
  public static final Ref APP_2   = Ref.ref(BookstoreApp.Attr.SPEC, "2");
  public static final Ref HTTPD_2 = Ref.ref(Apache.SPEC, "2");
  public static final Ref APP_3   = Ref.ref(BookstoreApp.Attr.SPEC, "3");
  public static final Ref HTTPD_3 = Ref.ref(Apache.SPEC, "3");
}
