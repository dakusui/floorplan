package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.utils.InternalUtils;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.InternalUtils.*;

public class InternalUtilsTest {
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

  @Test(expected = IllegalStateException.class)
  public void givenParallelStream$whenCollectBySingletonCollector$thenExceptionThrown() {
    Stream.of("hello", "world").parallel().collect(singletonCollector());
  }

  @Test
  public void givenParallelStreamWithOneElement$whenCollectBySingletonCollector$thenExceptionThrown() {
    assertThat(
        Stream.of("hello", "world").filter(s -> s.equals("world")).parallel().collect(singletonCollector()),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asString("get").equalTo("world").$()
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
        isInstanceOf(String.class).and(toPrintablePredicate(() -> "alwaysTrue", t -> true)),
        allOf(
            asString("toString").equalTo("and(assignableTo[String],alwaysTrue)").$(),
            asBoolean("test", "aStringObject").isTrue().$()
        ));
  }

  @Test
  public void givenORedPrintablePredicate$whenApplied$thenResultCorrectAndPrintedPretty() {
    assertThat(
        toPrintablePredicate(() -> "alwaysFalse", v -> false).or(isInstanceOf(String.class).and(toPrintablePredicate(() -> "alwaysTrue", t -> true))),
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
    InternalUtils.createWithNoParameterConstructor(ClassWithPrivateConstructor.class);
  }
}
