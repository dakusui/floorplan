package com.github.dakusui.floorplan.utils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.floorplan.exception.Exceptions.rethrow;

/**
 * A factory class to synthesize an implementation of a given interface (semi-)automatically.
 *
 * @param <T> A class of an interface for which an implementation is to be synthesized.
 */
public class ObjectSynthesizer<T> {
  private final Class<T>                                          anInterface;
  private final List<? extends Handler>                           handlers;
  private final Map<Method, BiFunction<Object, Object[], Object>> handlersCache;
  private final Object                                            fallbackObject;
  private final Map<Class, MethodHandles.Lookup>                  lookups;

  public static <T> ObjectSynthesizer.Builder<T> builder(Class<T> anInterface) {
    return new Builder<>(anInterface);
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

  public T synthesize() {
    return createProxy();
  }

  private ObjectSynthesizer(Class<T> anInterface, List<? extends Handler> handlers, Object fallbackObject) {
    this.anInterface = Objects.requireNonNull(anInterface);
    this.handlers = handlers;
    this.handlersCache = new HashMap<>();
    this.fallbackObject = fallbackObject;
    this.lookups = new HashMap<>();
    this.lookup(anInterface);
  }

  private MethodHandles.Lookup lookup(Class<? extends T> anInterface) {
    return this.lookups.computeIfAbsent(anInterface, ObjectSynthesizer::createLookup);
  }

  private static MethodHandles.Lookup createLookup(Class anInterface) {
    try {
      Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
      constructor.setAccessible(true);
      return constructor.newInstance(anInterface);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw rethrow(e);
    }
  }

  @SuppressWarnings({ "unchecked", "Convert2MethodRef" })
  private T createProxy() {
    return (T) Proxy.newProxyInstance(
        anInterface.getClassLoader(),
        new Class[] { anInterface },
        (proxy, method, args) -> handleMethodCall(proxy, method, args)
    );
  }

  private Object handleMethodCall(Object proxy, Method method, Object[] args) {
    return lookUpMethodCallHandler(method).orElseThrow(UnsupportedOperationException::new).apply(proxy, args);
  }

  private Optional<? extends BiFunction<Object, Object[], Object>> lookUpMethodCallHandler(Method method) {
    if (this.handlersCache.containsKey(method)) {
      return Optional.of(this.handlersCache.get(method));
    }
    Optional<? extends BiFunction<Object, Object[], Object>> ret = createMethodCallingFunction(method);
    if (ret.isPresent())
      this.handlersCache.put(method, ret.get());
    else
      this.handlersCache.put(method, null);
    return ret;
  }

  private Optional<? extends BiFunction<Object, Object[], Object>> createMethodCallingFunction(Method method) {
    Optional<? extends BiFunction<Object, Object[], Object>> ret = handlers.stream().filter(handler -> handler.test(method)).findFirst();
    return ret.isPresent() ?
        ret :
        Optional.of(createMethodCallingFunctionOnFallback(method));
  }

  private BiFunction<Object, Object[], Object> createMethodCallingFunctionOnFallback(Method method) {
    return (self, args) -> invokeMethod(self, fallbackObject, method, args);
  }

  @SuppressWarnings("unchecked")
  private Object invokeMethod(Object proxy, Object fallbackObject, Method method, Object[] args) {
    try {
      boolean wasAccessible = method.isAccessible();
      method.setAccessible(true);
      try {
        Class<?> declaringClass = method.getDeclaringClass();
        if (method.isDefault() && !isOverriddenIn(method, fallbackObject.getClass())) {
          return lookup((Class<? extends T>) declaringClass)
              .in(declaringClass)
              .unreflectSpecial(method, declaringClass)
              .bindTo(proxy)
              .invokeWithArguments(args);
        }
        return method.invoke(fallbackObject, args);
      } finally {
        method.setAccessible(wasAccessible);
      }
    } catch (Throwable e) {
      throw rethrow(e);
    }
  }

  private static boolean isOverriddenIn(Method method, Class<?> aClass) {
    if (method.getDeclaringClass() == aClass)
      return false;
    try {
      return aClass.getDeclaredMethod(method.getName(), method.getParameterTypes()) != null;
    } catch (NoSuchMethodException e) {
      return Stream.concat(
          Stream.of(aClass.getSuperclass()),
          Stream.of(aClass.getInterfaces()))
          .filter(Objects::nonNull)
          .anyMatch(k -> isOverriddenIn(method, k));
    }
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
      this.handle(
          // a default for 'equals' method.
          methodCall("equals", Object.class).with(
              (self, objects) -> self == objects[0] || fallbackObject.equals(objects[0])
          ));
      return new ObjectSynthesizer<>(this.anInterface, new ArrayList<>(handlers), fallbackObject);
    }
  }

  private static Handler.Builder methodCall(Predicate<Method> predicate) {
    return new Handler.Builder(predicate);
  }

  public interface Handler extends BiFunction<Object, Object[], Object>, Predicate<Method> {
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