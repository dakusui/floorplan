package com.github.dakusui.floorplan.exception;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.policy.Profile;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static java.lang.String.format;

public enum Exceptions {
  ;

  public static RuntimeException throwImpossibleLineReached() {
    throw throwImpossibleLineReached("This line is not expected to be executed");
  }

  public static RuntimeException throwImpossibleLineReached(String message) {
    throw throwExceptionForImpossibleLine(message);
  }

  public static RuntimeException throwImpossibleLineReached(String message, Throwable t) {
    throw throwExceptionForImpossibleLine(message, t);
  }

  public static RuntimeException throwExceptionForIllegalValue(String message) {
    throw new IllegalArgumentException(message);
  }

  public static RuntimeException throwExceptionForIllegalState(String message) {
    throw new IllegalStateException(message);
  }

  public static RuntimeException throwExceptionForNullValue(String message) {
    throw new NullPointerException(message);
  }

  private static RuntimeException throwExceptionForImpossibleLine(String message, Throwable t) {
    throw new AssertionError(message, t);
  }

  private static RuntimeException throwExceptionForImpossibleLine(String message) {
    throw new AssertionError(message);
  }

  private static RuntimeException throwExceptionForCaughtFailure(String message, Throwable t) {
    throw new RuntimeException(message, t);
  }

  public static RuntimeException throwUnsupportedOperation(String message) {
    throw new UnsupportedOperationException(message);
  }

  public static Supplier<RuntimeException> noSuchElement() {
    return noSuchElement("No such element");
  }

  public static Supplier<RuntimeException> noSuchElement(String format, Object... args) {
    return () -> {
      throw new NoSuchElementException(String.format(format, args));
    };
  }

  public static Supplier<RuntimeException> missingValue(Ref ref, Attribute attribute) {
    return missingValue(format("Missing value for attribute '%s' in component '%s'", attribute.name(), ref));
  }

  public static Supplier<RuntimeException> missingValue(String message) {
    return () -> {
      throw new MissingValueException(message);
    };
  }

  public static Supplier<RuntimeException> typeMismatch(Attribute attr, Object v) {
    return () -> {
      throw new TypeMismatch(String.format(
          "A value of '%s' was expected to satisfy '%s', but '%s'(%s) was given.",
          attr.name(),
          attr.describeConstraint(),
          v,
          v != null ?
              v.getClass().getCanonicalName() :
              "n/a"
      ));
    };
  }

  public static Supplier<RuntimeException> incompatibleProfile(FloorPlan floorPlan, Profile profile) {
    return () -> {
      throw new IncompatibleProfile(
          format(
              "Given profile is not compatible with the floorplan. (floorplan='%s', profile='%s')",
              floorPlan,
              profile
          ));
    };
  }

  public static RuntimeException rethrow(Throwable e) {
    if (e.getCause() != null)
      throw rethrow(e.getCause());
    if (e instanceof RuntimeException)
      throw (RuntimeException) e;
    if (e instanceof Error)
      throw (Error) e;
    throw new RuntimeException(e);
  }
}
