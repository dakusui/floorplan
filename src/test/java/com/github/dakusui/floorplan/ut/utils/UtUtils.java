package com.github.dakusui.floorplan.ut.utils;

import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import org.hamcrest.CoreMatchers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

import static org.junit.Assume.assumeThat;


public enum UtUtils {
  ;
  static final        PrintStream STDOUT = System.out;
  static final        PrintStream STDERR = System.err;
  public static final PrintStream NOP    = new PrintStream(new OutputStream() {
    @Override
    public void write(int b) {
    }
  });


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

  public static FloorPlan createUtFloorPlan() {
    return FloorPlan.create();// .add(ref(UtComponent.SPEC, "1"))
  }

  /**
   * Typically called from a method annotated with {@literal @}{@code Before} method.
   */
  public static void suppressStdOutErrIfRunUnderSurefireOrPitest() {
    if (isRunUnderSurefire() || isRunUnderPitest()) {
      System.setOut(NOP);
      System.setErr(NOP);
    }
  }

  private static boolean isRunUnderPitest() {
    return Objects.equals(System.getProperty("underpitest", "no"), "yes");
  }

  /**
   * Typically called from a method annotated with {@literal @}{@code After} method.
   */
  public static void restoreStdOutErr() {
    System.setOut(STDOUT);
    System.setErr(STDERR);
  }

  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  /**
   * Call this method from a test which is known to be passing under normal condition
   * (i.e. under normal maven's surefire or under your IDE) but not under
   * pitest.
   */
  public static void assumeThatNotUnderPitest() {
    assumeThat(
        isRunUnderPitest(),
        CoreMatchers.equalTo(false)
    );
  }
}
