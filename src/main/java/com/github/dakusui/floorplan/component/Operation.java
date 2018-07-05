package com.github.dakusui.floorplan.component;

/**
 * This enumerates categories of actions that can be performed on a component.
 */
public enum Operation {
  /**
   * An operation that belongs to this category installs a component from which
   * it is created.
   */
  INSTALL,
  /**
   * An operation that belongs to this category starts a component from which
   * it is created.
   */
  START,
  /**
   * An operation that belongs to this category stops a component from which
   * it is created.
   */
  STOP,
  /**
   * An operation that belongs to this category kills a component from which
   * it is created.
   */
  NUKE,
  /**
   * An operation that belongs to this category uninstalls a component from which
   * it is created.
   */
  UNINSTALL
}
