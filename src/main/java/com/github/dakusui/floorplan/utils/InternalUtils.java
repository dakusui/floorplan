package com.github.dakusui.floorplan.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.crest.utils.printable.Predicates;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dakusui.floorplan.exception.Exceptions.inconsistentSpec;
import static com.github.dakusui.floorplan.utils.Checks.*;

public class InternalUtils {
  ;

  public static <T> Collector<T, List<T>, Optional<T>> singletonCollector() {
    return Collector.of(
        ArrayList::new,
        (ts, t) -> {
          if (ts.isEmpty()) {
            ts.add(t);
            return;
          }
          throw new IllegalStateException();
        },
        (left, right) -> {
          if (left.size() == 1 && right.isEmpty() || left.isEmpty() && right.size() == 1) {
            left.addAll(right);
            return left;
          }
          throw new IllegalStateException();
        },
        list -> {
          require(list, l -> l.size() <= 1, l -> IllegalArgumentException::new);
          return list.isEmpty() ?
              Optional.empty() :
              Optional.of(list.get(0));
        }
    );
  }

  public static void performAction(Action action) {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
  }

  public static Context newContext() {
    return Context.create();
  }

  public static <T, R> Function<T, R> toPrintableFunction(Supplier<String> messageSupplier, Function<T, R> func) {
    return new Function<T, R>() {
      @Override
      public R apply(T t) {
        return func.apply(t);
      }

      @Override
      public String toString() {
        return messageSupplier.get();
      }
    };
  }

  public static <T> Predicate<T> toPrintablePredicate(Supplier<String> messageSupplier, Predicate<T> pred) {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return pred.test(t);
      }

      @Override
      public Predicate<T> negate() {
        return toPrintablePredicate(() -> String.format("!%s", this.toString()), pred.negate());
      }

      @Override
      public Predicate<T> and(Predicate<? super T> another) {
        return toPrintablePredicate(() -> String.format("and(%s,%s)", this.toString(), another.toString()), pred.and(another));
      }

      @Override
      public Predicate<T> or(Predicate<? super T> another) {
        return toPrintablePredicate(() -> String.format("or(%s,%s)", this.toString(), another.toString()), pred.or(another));
      }


      @Override
      public String toString() {
        return messageSupplier.get();
      }
    };
  }

  public static Predicate<Object> isInstanceOf(Class<?> expectedType) {
    return InternalUtils.toPrintablePredicate(
        () -> String.format("assignableTo[%s]", expectedType.getSimpleName()),
        expectedType.isPrimitive() ?
            v -> v != null && expectedType.isAssignableFrom(v.getClass()) :
            v -> v == null || expectedType.isAssignableFrom(v.getClass())
    );
  }

  public static <A extends Attribute> Predicate<Object> hasCompatibleSpecWith(ComponentSpec<A> spec) {
    return InternalUtils.toPrintablePredicate(
        () -> String.format("hasCompatibleSpecWith[%s]", spec),
        (Object v) -> parentsOf(((Ref) v).spec()).contains(spec)
    );
  }

  @SuppressWarnings("unchecked")
  private static List<ComponentSpec> parentsOf(ComponentSpec spec) {
    List<ComponentSpec> ret = new LinkedList<>();
    ret.add(spec);
    Optional<ComponentSpec> cur = spec.parentSpec();
    while (cur.isPresent()) {
      ret.add(cur.get());
      cur = cur.get().parentSpec();
    }
    return ret;
  }


  @SuppressWarnings({ "unchecked" })
  public static Predicate<Object> forAll(Predicate<Object> pred) {
    return InternalUtils.toPrintablePredicate(
        () -> String.format("allMatch[%s]", pred),
        (Object v) -> List.class.cast(v).stream().allMatch(pred)
    );
  }

  @SuppressWarnings("unchecked")
  private static <A> A getStaticFieldValue(Field field) {
    try {
      boolean wasAccessible = field.isAccessible();
      field.setAccessible(true);
      try {
        return (A) field.get(null);
      } finally {
        field.setAccessible(wasAccessible);
      }
    } catch (IllegalAccessException e) {
      throw Exceptions.rethrow(e);
    }
  }

  public static <T> T createWithNoParameterConstructor(Class<T> tClass) {
    try {
      return requireNonNull(tClass).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw Exceptions.rethrow(e);
    }
  }

  public static <A extends Attribute> String determineAttributeName(Class<? extends A> attributeClass, A attribute) {
    return attributeFields(attributeClass).stream()
        .filter(s -> Objects.equals(InternalUtils.getStaticFieldValue(s), attribute))
        .map(Field::getName)
        .findFirst().orElseThrow(RuntimeException::new);
  }

  private static <A extends Attribute> List<Field> attributeFields(Class<A> attrType) {
    return Arrays.stream(attrType.getFields())
        .filter(field -> Modifier.isStatic(field.getModifiers()))
        .filter(field -> Modifier.isFinal(field.getModifiers()))
        .filter(field -> Attribute.class.isAssignableFrom(attrType))
        .filter(field -> field.getType().isAssignableFrom(field.getType()))
        .sorted(Comparator.comparing(Field::getName))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public static List<Attribute> attributes(Class<? extends Attribute> attrType) {
    return new LinkedList<Attribute>() {
      {
        addAll(
            Arrays.stream(attrType.getFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .filter(field -> Attribute.class.isAssignableFrom(field.getType()))
                .filter(field -> Attribute.class.isAssignableFrom(attrType))
                .sorted(Comparator.comparing(Field::getName))
                .map(field -> (Attribute) getStaticFieldValue(field))
                .collect((Collector<Attribute, Map<String, Attribute>, Map<String, Attribute>>) Collector.class.cast(attributeCollector())
                ).values()
        );
      }
    };
  }

  private static <A extends Attribute> Collector<A, Map<String, A>, Map<String, A>> attributeCollector() {
    return Collector.of(
        LinkedHashMap::new,
        (map, attr) -> {
          String key = attr.name();
          updateMapWithGivenAttributeIfNecessary(map, key, attr);
        },
        (Map<String, A> mapA, Map<String, A> mapB) -> new LinkedHashMap<String, A>() {{
          putAll(mapA);
          Map<String, A> map = this;
          mapB.forEach((key, attr) -> updateMapWithGivenAttributeIfNecessary(map, key, attr));
        }});
  }

  private static <A extends Attribute> void updateMapWithGivenAttributeIfNecessary(Map<String, A> map, String key, A attr) {
    if (map.containsKey(key))
      map.put(key,
          attr.moreSpecialized(map.get(key)).orElseThrow(
              inconsistentSpec(Exceptions.inconsistentSpecMessageSupplier(map.get(key), attr)))
      );
    else
      map.put(key, attr);
  }

  public static String shortenedClassName(Class klass) {
    if (klass.getEnclosingClass() == null)
      return klass.getCanonicalName().replaceAll("[a-zA-Z0-9]+\\.", "");
    return shortenedClassName(klass.getEnclosingClass()) + "." + klass.getSimpleName();

  }

  public static <T, U> BiPredicate<T, U> printableBiPredicate(Supplier<String> formatter, BiPredicate<T, U> biPredicate) {
    return new BiPredicate<T, U>() {
      @Override
      public boolean test(T t, U u) {
        return biPredicate.test(t, u);
      }

      @Override
      public String toString() {
        return formatter.get();
      }

      @Override
      public BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new BiPredicate<T, U>() {
          @Override
          public boolean test(T t, U u) {
            return biPredicate.test(t, u) && other.test(t, u);
          }

          @Override
          public String toString() {
            return String.format("(%s)&&(%s)", formatter.get(), other.toString());
          }
        };
      }
    };
  }

  public static Predicate<Object> isEqualTo(Object value) {
    return toPrintablePredicate(() -> String.format("isEqualTo[%s]", value), v -> Objects.equals(v, value));
  }

  @SuppressWarnings("unchecked")
  public static <A extends Attribute> Class<A> figureOutAttributeTypeFor(Class<? extends Component<A>> componentType) {
    Supplier<String> messageSupplier = () -> String.format("Given class '%s' doesn't seem to be a valid component.", componentType.toGenericString());
    return Class.class.cast(requireArgument(
        ParameterizedType.class.cast(requireArgument(
            requireArgument(
                componentType.getGenericInterfaces(),
                v -> v.length == 1,
                messageSupplier
            )[0],
            v -> v instanceof ParameterizedType,
            messageSupplier)
        ).getActualTypeArguments(),
        v -> v.length == 1,
        messageSupplier)[0]
    );
  }
}
