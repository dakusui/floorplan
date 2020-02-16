package com.github.dakusui.floorplan.utils;

import com.github.dakusui.osynth.MethodHandler;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * A class for backward compatibility.
 * Use {@code com.github.dakusui.osynth.ObjectSynthesizer} directly.
 * @param <T> A type of object to be synthesized.
 * @deprecated use
 */
@Deprecated
public class ObjectSynthesizer<T> {
  private final Builder<T> builder;

  ObjectSynthesizer(Builder<T> builder) {
    this.builder = builder;
  }

  public static <T> ObjectSynthesizer.Builder<T> builder(Class<T> anInterface) {
    return new Builder<>(anInterface);
  }

  public static MethodHandler.Builder methodCall(String methodName, Class<?>... parameterTypes) {
    return com.github.dakusui.osynth.ObjectSynthesizer.methodCall(methodName, parameterTypes);
  }

  public T synthesize() {
    return this.builder.synthesize();
  }

  public static class Builder<T> extends com.github.dakusui.osynth.SimpleObjectSynthesizer<T> {
    public Builder(Class<T> anInterface) {
      super(anInterface);
    }

    @Override
    public Builder<T> handle(MethodHandler handler) {
      return (Builder<T>) super.handle(handler);
    }

    @Override
    public Builder<T> fallbackTo(Object fallbackObject) {
      return (Builder<T>) super.fallbackTo(fallbackObject);
    }

    public ObjectSynthesizer<T> build() {
      return new ObjectSynthesizer<>(this);
    }
  }

  public interface Handler extends MethodHandler, Predicate<Method> {
    class Builder extends MethodHandler.Builder {
      public Builder(Predicate<Method> matcher) {
        super(matcher);
      }
    }
  }
}
