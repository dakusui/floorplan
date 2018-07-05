package com.github.dakusui.floorplan.utils;

import com.github.dakusui.floorplan.exception.Exceptions;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;

public enum Checks {
  ;

  public static <T> T requireArgument(T value, Predicate<T> condition) {
    return requireArgument(value, condition, v -> format("'%s' did not not satisfy <%s>", v, condition));
  }

  public static <T> T requireArgument(T value, Predicate<T> condition, Function<T, String> messageComposer) {
    return requireArgument(value, condition, () -> messageComposer.apply(value));
  }

  public static <T> T requireArgument(T value, Predicate<T> condition, Supplier<String> messageComposer) {
    if (condition.test(value))
      return value;
    throw Exceptions.throwExceptionForIllegalValue(messageComposer.get());
  }

  public static <T> T requireState(T value, Predicate<T> condition) {
    return requireState(value, condition, v -> format("'%s' did not not satisfy <%s>", v, condition));
  }

  public static <T> T requireState(T value, Predicate<T> condition, Function<T, String> messageComposer) {
    if (condition.test(value))
      return value;
    throw Exceptions.throwExceptionForIllegalState(messageComposer.apply(value));
  }

  public static <T, E extends RuntimeException> T require(T value, Predicate<T> condition, Supplier<E> exceptionThrower) {
    if (condition.test(value))
      return value;
    throw exceptionThrower.get();
  }

  public static <T> T requireNonNull(T value) {
    return requireNonNull(value, () -> null);
  }

  public static <T> T requireNonNull(T value, String message) {
    return requireNonNull(value, () -> message);
  }

  public static <T> T requireNonNull(T value, Supplier<String> messageSupplier) {
    if (value != null)
      return value;
    throw Exceptions.throwExceptionForNullValue(messageSupplier.get());
  }

}
