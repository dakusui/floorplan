package com.github.dakusui.floorplan.ut.utils;

import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;

import java.util.function.Function;
import java.util.function.Supplier;

public enum UtUtils {
  ;

  /**
   * A method to pretend to run a shell command. This just prints out given format
   * and args using the {@code String.format} method.
   * <p>
   * Note that this method automatically adds a carriage return to the end of the line.
   *
   * @param fmt  A format of the string to be printed.
   * @param args Arguments to be embedded in a format string {@code fmt}.
   */
  public static void runShell(String fmt, Object... args) {
    System.out.println(String.format(fmt, args));
  }

  public static Policy buildPolicy(FloorPlan floorPlan, ComponentSpec<?>... specs) {
    Policy.Builder builder = new Policy.Builder();
    for (ComponentSpec<?> each : specs) {
      builder = builder.addComponentSpec(each);
    }
    return builder.setFloorPlan(floorPlan).setProfile(new SimpleProfile()).build();
  }

}
