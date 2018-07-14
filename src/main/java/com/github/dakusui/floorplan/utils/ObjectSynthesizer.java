package com.github.dakusui.floorplan.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.floorplan.utils.ObjectSynthesizer.Default.methodCall;

/**
 * A factory class to synthesize an implementation of a given interface (semi-)automatically.
 *
 * @param <T> A class of an interface for which an implementation is to be synthesized.
 */
public abstract class ObjectSynthesizer<T> {
  private final Class<T> anInterface;

  protected ObjectSynthesizer(Class<T> anInterface) {
    this.anInterface = Objects.requireNonNull(anInterface);
  }

  public T synthesize() {
    //noinspection unchecked
    return (T) Proxy.newProxyInstance(
        anInterface.getClassLoader(),
        new Class[] { anInterface },
        (proxy, method, args) -> handleMethodCall(method, args)
    );
  }

  protected Object handleMethodCall(Method method, Object[] args) {
    return lookUpMethodCallHandler(method).orElseThrow(UnsupportedOperationException::new).apply(args);
  }

  public static <T> ObjectSynthesizer.Default.Builder<T> builder(Class<T> anInterface) {
    return new Default.Builder<T>(anInterface);
  }

  abstract protected Optional<? extends Function<Object[], Object>> lookUpMethodCallHandler(Method method);

  public static class Default<T> extends ObjectSynthesizer<T> {

    private final List<? extends Handler> handlers;
    private final Object                  fallbackObject;

    protected Default(Class<T> anInterface, List<? extends Handler> handlers, Object fallbackObject) {
      super(anInterface);
      this.handlers = handlers;
      this.fallbackObject = fallbackObject;
    }

    @Override
    protected Optional<? extends Function<Object[], Object>> lookUpMethodCallHandler(Method method) {
      Optional<? extends Function<Object[], Object>> ret = handlers.stream().filter(handler -> handler.test(method)).findFirst();
      return ret.isPresent() ?
          ret :
          Optional.of(_lookUpMethodCallHandler(method));
    }

    private Function<Object[], Object> _lookUpMethodCallHandler(Method method) {
      return args -> invokeMethod(fallbackObject, method, args);
    }

    private Object invokeMethod(Object object, Method method, Object[] args) {
      try {
        return method.invoke(object, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    public static Handler.Builder methodCall(String methodName, Class<?>... parameterTypes) {
      return methodCall(method -> {
        AtomicInteger i = new AtomicInteger(-1);
        return Objects.equals(methodName, method.getName()) &&
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

    public static Handler.Builder methodCall(Predicate<Method> predicate) {
      return new Handler.Builder(predicate);
    }

    public static class Builder<T> {
      private final Class<T> anInterface;
      private       Object   fallbackObject;
      private List<Handler> handlers = new LinkedList<>();

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
        return build().synthesize();
      }
    }
  }

  interface A {
    String aMethod();
  }

  interface B {
    String bMethod();
  }

  interface C {
    String cMethod();
  }

  interface X extends A, B, C {
    String xMethod();
  }

  interface Handler extends Function<Object[], Object>, Predicate<Method> {
    class Builder {
      private final Predicate<Method>          matcher;
      private       Function<Object[], Object> function;

      public Builder(Predicate<Method> matcher) {
        this.matcher = Objects.requireNonNull(matcher);
      }

      public Handler with(Function<Object[], Object> function) {
        this.function = Objects.requireNonNull(function);
        return this.build();
      }

      public Handler build() {
        return new Handler() {
          @Override
          public Object apply(Object[] objects) {
            return function.apply(objects);
          }

          @Override
          public boolean test(Method method) {
            return matcher.test(method);
          }
        };
      }
    }
  }


  public static void main(String... _args) {
    Object fallbackObject = new X() {
      @Override
      public String xMethod() {
        return "xMethod";
      }

      @Override
      public String cMethod() {
        return "cMethod";
      }

      @Override
      public String bMethod() {
        return "bMethod";
      }

      @Override
      public String aMethod() {
        return "aMethod";
      }
    };
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with(args -> "a is called"))
        .handle(methodCall("bMethod").with(args -> "b is called"))
        .fallbackTo(fallbackObject)
        .synthesize();
    System.out.println(x.aMethod());
    System.out.println(x.bMethod());
    System.out.println(x.toString());
    System.out.println(x.cMethod());
    System.out.println(x.xMethod());
  }
}