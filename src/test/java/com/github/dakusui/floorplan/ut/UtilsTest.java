package com.github.dakusui.floorplan.ut;

import org.junit.Test;

import java.util.Collections;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.Utils.singletonCollector;

public class UtilsTest {
  @Test
  public void givenStreamContainingOnlyOneElement$whenCollectBySingletonCollector$thenValueReturned() {
    assertThat(
        Stream.of("hello").collect(singletonCollector()),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asString("get").equalTo("hello").$()
        )
    );
  }

  @Test
  public void givenEmptyStream$whenCollectBySingletonCollector$thenEmptyReturned() {
    assertThat(
        Stream.empty().collect(singletonCollector()),
        allOf(
            asBoolean("isPresent").isFalse().$()
        )
    );
  }

  @Test(expected = IllegalStateException.class)
  public void givenStreamContainingMultipleElements$whenCollectBySingletonCollector$thenExceptionThrown() {
    System.out.printf("%s%n", Stream.of("hello", "world").collect(singletonCollector()));
  }
}
