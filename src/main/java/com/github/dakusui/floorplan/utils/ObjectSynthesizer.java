package com.github.dakusui.floorplan.utils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

@Deprecated
public class ObjectSynthesizer<T> extends com.github.dakusui.objsynth.ObjectSynthesizer.ObjectFactory<T> {

  protected ObjectSynthesizer(Class<T> anInterface, List<? extends com.github.dakusui.objsynth.ObjectSynthesizer.MethodHandler> handlers, Object fallbackObject) {
    super(anInterface, handlers, fallbackObject);
  }

  public static <T> ObjectSynthesizer.Builder<T> builder(Class<T> anInterface) {
    return new Builder<>(anInterface);
  }

  public static com.github.dakusui.objsynth.ObjectSynthesizer.MethodHandler.Builder methodCall(String methodName, Class<?>... parameterTypes) {
    return com.github.dakusui.objsynth.ObjectSynthesizer.methodCall(methodName, parameterTypes);
  }

  public interface Handler extends com.github.dakusui.objsynth.ObjectSynthesizer.MethodHandler, Predicate<Method> {
    class Builder extends com.github.dakusui.objsynth.ObjectSynthesizer.MethodHandler.Builder {
      public Builder(Predicate<Method> matcher) {
        super(matcher);
      }
    }
  }

  public static class Builder<T> extends com.github.dakusui.objsynth.ObjectSynthesizer<T> {
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

    @Override
    public ObjectFactory<T> build() {
      return super.build();
    }
  }
}
