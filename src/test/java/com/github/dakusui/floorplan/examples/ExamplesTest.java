package com.github.dakusui.floorplan.examples;

import com.github.dakusui.floorplan.examples.bookstore.AnotherBookstoreExample;
import com.github.dakusui.floorplan.examples.bookstore.BookstoreExample;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import static com.github.dakusui.crest.Crest.*;

public class ExamplesTest {
  @Test
  public void givenBookstoreExample$whenExecuteTests$thenAllPass() {
    assertThat(
        JUnitCore.runClasses(BookstoreExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(10).$(),
            asInteger("getFailureCount").equalTo(0).$(),
            asInteger("getIgnoreCount").equalTo(0).$()
        )
    );
  }

  @Test
  public void givenAnotherBookstoreExample$whenExecuteTests$thenAllPass() {
    assertThat(
        JUnitCore.runClasses(AnotherBookstoreExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(4).$(),
            asInteger("getFailureCount").equalTo(0).$(),
            asInteger("getIgnoreCount").equalTo(0).$()
        )
    );
  }

}
