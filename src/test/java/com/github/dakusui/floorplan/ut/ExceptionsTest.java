package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.exception.IncompatibleProfile;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

import static com.github.dakusui.crest.Crest.*;

public class ExceptionsTest {
  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void givenArrayIndexOutOfBoundsExceptionNestedInRuntimeException$whenRethrow$thenUnwrapped() {
    throw Exceptions.rethrow(new RuntimeException(new ArrayIndexOutOfBoundsException()));
  }

  @Test(expected = OutOfMemoryError.class)
  public void givenOutOfMemoryErrorNestedInRuntimeException$whenRethrow$thenUnwrapped() {
    throw Exceptions.rethrow(new RuntimeException(new OutOfMemoryError()));
  }

  @SuppressWarnings("ThrowableNotThrown")
  @Test(expected = RuntimeException.class)
  public void givenIOExceptionNestedInRuntimeException$whenRethrow$thenUnwrapped() {
    try {
      Exceptions.rethrow(new RuntimeException(new RuntimeException(new IOException())));
    } catch (RuntimeException e) {
      // Exception must be thrown, even if it's not thrown by a programmer
      assertThat(
          e,
          allOf(
              // Thrown Exception must be exactly a RuntimeException, not a subclass of it.
              asObject("getClass").equalTo(RuntimeException.class).$(),
              asObject(call("getCause").andThen("getClass").$()).equalTo(IOException.class).$()
          )
      );
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenThrowExceptionForIllegalValue$thenThrown() {
    RuntimeException exception = Exceptions.throwExceptionForIllegalValue("");
    exception.printStackTrace();
  }

  @Test(expected = IllegalStateException.class)
  public void whenThrowExceptionForIllegalState$thenThrown() {
    RuntimeException exception = Exceptions.throwExceptionForIllegalState("");
    exception.printStackTrace();
  }

  @Test(expected = NullPointerException.class)
  public void whenThrowExceptionForNullValue$thenThrown() {
    RuntimeException exception = Exceptions.throwExceptionForNullValue("");
    exception.printStackTrace();
  }

  @Test(expected = NoSuchElementException.class)
  public void whenNoSuchElement$thenThrown() {
    RuntimeException exception = Exceptions.noSuchElement().get();
    exception.printStackTrace();
  }

  @Test(expected = IncompatibleProfile.class)
  public void whenIncompatibleProfile$thenThrown() {
    RuntimeException exception = Exceptions.incompatibleProfile(null, null).get();
    exception.printStackTrace();
  }

}
