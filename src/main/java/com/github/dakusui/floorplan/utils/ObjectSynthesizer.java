package com.github.dakusui.floorplan.utils;

import com.github.dakusui.osynth.core.FallbackHandlerFactory;
import com.github.dakusui.osynth.core.MethodHandler;
import com.github.dakusui.osynth.core.ProxyDescriptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

/**
 * A class for backward compatibility.
 * Use {@code com.github.dakusui.osynth.ObjectSynthesizer} directly.
 * @param <T> A type of object to be synthesized.
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
    private boolean fallbackIsSet = false;

    public Builder(Class<T> anInterface) {
      super(anInterface);
    }

    @Override
    public Builder<T> handle(MethodHandler handler) {
      return (Builder<T>) super.handle(handler);
    }

    public Builder<T> fallbackTo(Object fallbackObject) {
      fallbackIsSet = true;
      return (Builder<T>) super.addHandlerObject(fallbackObject);
    }

    public ObjectSynthesizer<T> build() {
      if (!fallbackIsSet)
        this.addHandlerObject(new Object());
      return new ObjectSynthesizer<>(this);
    }

    @Override
    protected ProxyDescriptor createProxyDescriptor(List<Class<?>> interfaces, List<MethodHandler> handlers, List<Object> handlerObjects, FallbackHandlerFactory fallbackHandlerFactory) {
      return new ProxyDescriptor(interfaces, handlers, handlerObjects, fallbackHandlerFactory) {
        @Override
        public boolean equals(Object anotherObject) {
          return super.equals(anotherObject) || this.handlerObjects().contains(anotherObject);
        }
      };
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
