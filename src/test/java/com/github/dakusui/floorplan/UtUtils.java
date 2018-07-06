package com.github.dakusui.floorplan;

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
}
