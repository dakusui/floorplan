package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.utils.Utils;
import org.junit.Test;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.Utils.*;

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

  @Test
  public void givenPrintablePredicate$whenApplied$thenResultCorrectAndPrintedPretty() {
    assertThat(
        isInstanceOf(String.class),
        allOf(
            asString("toString").equalTo("assignableTo[String]").$(),
            asBoolean("test", Object.class).isFalse().$()
        ));
  }

  @Test
  public void givenNegatedPrintablePredicate$whenApplied$thenResultCorrectAndPrintedPretty() {
    assertThat(
        isInstanceOf(String.class).negate(),
        allOf(
            asString("toString").equalTo("!assignableTo[String]").$(),
            asBoolean("test", Object.class).isTrue().$()
        ));
  }

  @Test
  public void givenAndedPrintablePredicate$whenApplied$thenResultCorrectAndPrintedPretty() {
    assertThat(
        isInstanceOf(String.class).and(toPrintable(() -> "alwaysTrue", (Predicate<Object>) t -> true)),
        allOf(
            asString("toString").equalTo("and(assignableTo[String],alwaysTrue)").$(),
            asBoolean("test", "aStringObject").isTrue().$()
        ));
  }

  @Test
  public void givenORedPrintablePredicate$whenApplied$thenResultCorrectAndPrintedPretty() {
    assertThat(
        toPrintable(() -> "alwaysFalse", (Predicate<Object>) v -> false).or(isInstanceOf(String.class).and(toPrintable(() -> "alwaysTrue", (Predicate<Object>) t -> true))),
        allOf(
            asString("toString").equalTo("or(alwaysFalse,and(assignableTo[String],alwaysTrue))").$(),
            asBoolean("test", "aStringObject").isTrue().$()
        ));
  }

  public static class ClassWithPrivateConstructor {
    private ClassWithPrivateConstructor() {
    }
  }

  @Test(expected = RuntimeException.class)
  public void givenPrivateConstructor$whenCreateWithNoParameterConstructor$thenFail() {
    Utils.createWithNoParameterConstructor(ClassWithPrivateConstructor.class);
  }
}
