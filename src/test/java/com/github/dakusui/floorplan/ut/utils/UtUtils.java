package com.github.dakusui.floorplan.ut.utils;

import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;

import java.util.function.Function;
import java.util.function.Supplier;

public enum UtUtils {
  ;

  /**
   * Note that this method automatically adds a carriage return to the end of the line.
   *
   * @param fmt  A format of the string to be printed.
   * @param args Arguments to be embedded in a format string {@code fmt}.
   */
  public static void printf(String fmt, Object... args) {
    System.out.println(String.format(fmt, args));
  }

  public static Policy buildPolicy(FloorPlan floorPlan, ComponentSpec<?>... specs) {
    Policy.Builder builder = new Policy.Builder();
    for (ComponentSpec<?> each : specs) {
      builder = builder.addComponentSpec(each);
    }
    return builder.setFloorPlan(floorPlan).setProfile(new SimpleProfile()).build();
  }

  public static <T, R> Function<T, R> toPrintable(Supplier<String> messageSupplier, Function<T, R> func) {
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
}
