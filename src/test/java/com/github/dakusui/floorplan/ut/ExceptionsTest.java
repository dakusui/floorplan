package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.exception.IncompatibleProfile;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

import static com.github.dakusui.crest.Crest.*;

public class ExceptionsTest {
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
    RuntimeException exception = Exceptions.incompatibleProfile(new SimpleProfile(), p -> false).get();
    exception.printStackTrace();
  }
}
