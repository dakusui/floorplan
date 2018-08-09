package com.github.dakusui.floorplan.exception;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;

import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.lang.String.format;

public enum Exceptions {
  ;

  public static RuntimeException throwExceptionForIllegalValue(String message) {
    throw new IllegalArgumentException(message);
  }

  public static RuntimeException throwExceptionForIllegalState(String message) {
    throw new IllegalStateException(message);
  }

  public static RuntimeException throwExceptionForNullValue(String message) {
    throw new NullPointerException(message);
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

  public static Supplier<RuntimeException> missingValue(Ref ref, String attrName) {
    return missingValue(format("Missing value for attribute '%s' in component '%s'", attrName, ref));
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


  public static Supplier<RuntimeException> typeMismatch(Class type, Object v) {
    return () -> {
      throw new TypeMismatch(String.format(
          "Given value '%s'(%s), was expected to be of '%s', but not.",
          v,
          v != null ?
              v.getClass().getCanonicalName() :
              "n/a",
          type.getCanonicalName()
      ));
    };
  }

  public static Supplier<RuntimeException> incompatibleProfile(Profile p, Predicate<Profile> profileRequirement) {
    return () -> {
      throw new IncompatibleProfile(
          String.format("Given profile '%s'(%s) did not meet requirement %s", p, p.getClass().getCanonicalName(), profileRequirement));
    };
  }

  public static Supplier<RuntimeException> inconsistentSpec(Supplier<String> messageSupplier) {
    return () -> {
      throw new InconsistentSpec(messageSupplier.get());
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

  public static <A extends Attribute> Supplier<String> inconsistentSpecMessageSupplier(A attr1, A attr2) {
    return () -> format(
        "It cannot be determined which is more special between '%s'(%s) and '%s'(%s)",
        attr2,
        attr2.getClass(),
        attr1,
        attr1.getClass()
    );
  }
}
