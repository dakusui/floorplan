package com.github.dakusui.floorplan.ut.utils;

import org.junit.After;
import org.junit.Before;

public abstract class UtBase {
  @Before
  public void before() {
    UtUtils.suppressStdOutErrIfRunUnderSurefire();
  }

  @After
  public void after() {
    UtUtils.restoreStdOutErr();
  }
}