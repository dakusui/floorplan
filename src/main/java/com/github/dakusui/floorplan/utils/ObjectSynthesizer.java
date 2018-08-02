package com.github.dakusui.floorplan.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A factory class to synthesize an implementation of a given interface (semi-)automatically.
 *
 * @param <T> A class of an interface for which an implementation is to be synthesized.
 */
public abstract class ObjectSynthesizer<T> {
  private final Class<T> anInterface;

  ObjectSynthesizer(Class<T> anInterface) {
    this.anInterface = Objects.requireNonNull(anInterface);
  }

  @SuppressWarnings({ "unchecked", "Convert2MethodRef" })
  private T synthesize() {
    return (T) Proxy.newProxyInstance(
        anInterface.getClassLoader(),
        new Class[] { anInterface },
        (proxy, method, args) -> handleMethodCall(proxy, method, args)
    );
  }

  private Object handleMethodCall(Object self, Method method, Object[] args) {
    return lookUpMethodCallHandler(method).orElseThrow(UnsupportedOperationException::new).apply(self, args);
  }

  public static <T> ObjectSynthesizer.Default.Builder<T> builder(Class<T> anInterface) {
    return new Default.Builder<>(anInterface);
  }

  abstract protected Optional<? extends BiFunction<Object, Object[], Object>> lookUpMethodCallHandler(Method method);

  public static class Default<T> extends ObjectSynthesizer<T> {

    private final List<? extends Handler> handlers;
    private final Object                  fallbackObject;

    Default(Class<T> anInterface, List<? extends Handler> handlers, Object fallbackObject) {
      super(anInterface);
      this.handlers = handlers;
      this.fallbackObject = fallbackObject;
    }

    @Override
    protected Optional<? extends BiFunction<Object, Object[], Object>> lookUpMethodCallHandler(Method method) {
      Optional<? extends BiFunction<Object, Object[], Object>> ret = handlers.stream().filter(handler -> handler.test(method)).findFirst();
      return ret.isPresent() ?
          ret :
          Optional.of(_lookUpMethodCallHandler(method));
    }

    private BiFunction<Object, Object[], Object> _lookUpMethodCallHandler(Method method) {
      return (self, args) -> invokeMethod(fallbackObject, method, args);
    }

    private Object invokeMethod(Object self, Method method, Object[] args) {
      try {
        boolean wasAccessible = method.isAccessible();
        try {
          method.setAccessible(true);
          return method.invoke(self, args);
        } finally {
          method.setAccessible(wasAccessible);
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    public static Handler.Builder methodCall(String methodName, Class<?>... parameterTypes) {
      return methodCall(method -> {
        AtomicInteger i = new AtomicInteger(-1);
        return Objects.equals(
            methodName, method.getName()) &&
            parameterTypes.length == method.getParameterCount() &&
            Arrays.stream(
                parameterTypes
            ).peek(
                type -> i.getAndIncrement()
            ).allMatch(
                type -> type.isAssignableFrom(method.getParameterTypes()[i.get()])
            );
      });
    }

    static Handler.Builder methodCall(Predicate<Method> predicate) {
      return new Handler.Builder(predicate);
    }

    public static class Builder<T> {
      private final Class<T>      anInterface;
      private       Object        fallbackObject;
      private       List<Handler> handlers = new LinkedList<>();

      public Builder(Class<T> anInterface) {
        this.anInterface = anInterface;
      }

      public Builder<T> fallbackTo(Object fallbackObject) {
        this.fallbackObject = fallbackObject;
        return this;
      }

      public Builder<T> handle(Handler handler) {
        handlers.add(handler);
        return this;
      }

      public ObjectSynthesizer<T> build() {
        return new Default<>(this.anInterface, new ArrayList<>(handlers), fallbackObject);
      }

      public T synthesize() {
        return this.handle(
            // a default for 'equals' method. If and only if given args is the same object
            // as itself ('this'), true will be returned.
            methodCall("equals", Object.class).with(
                (self, objects) -> self == objects[0]
            )
        ).build(
        ).synthesize();
      }
    }
  }

  interface Handler extends BiFunction<Object, Object[], Object>, Predicate<Method> {
    class Builder {
      private final Predicate<Method>                    matcher;
      private       BiFunction<Object, Object[], Object> function;

      public Builder(Predicate<Method> matcher) {
        this.matcher = Objects.requireNonNull(matcher);
      }

      public Handler with(BiFunction<Object, Object[], Object> function) {
        this.function = Objects.requireNonNull(function);
        return this.build();
      }

      public Handler build() {
        return new Handler() {
          @Override
          public Object apply(Object self, Object[] objects) {
            return function.apply(self, objects);
          }

          @Override
          public boolean test(Method method) {
            return matcher.test(method);
          }
        };
      }
    }
  }
}