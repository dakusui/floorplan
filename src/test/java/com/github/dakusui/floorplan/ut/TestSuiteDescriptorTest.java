package com.github.dakusui.floorplan.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import com.github.dakusui.floorplan.ut.tdesc.UtTsDescProfile;
import com.github.dakusui.floorplan.ut.tdesc.UtTsDescriptorFactory;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.Utils.toPrintable;

public class TestSuiteDescriptorTest {
  @Test
  public void givenUtTestSuiteDesc$when$then() {
    Context context = Utils.newContext();
    assertThat(
        new UtTsDescriptorFactory().create(new UtTsDescProfile()),
        allOf(
            asString(toPrintable(() -> "getName", TestSuiteDescriptor::getName)).equalTo("UtTsDesc").$(),
            asInteger("size").equalTo(2).$(),
            asString("getTestCaseNameFor", 0).equalTo("UtTsDescCase[00]").$(),
            asString(toPrintable(
                () -> "setUpFirstTime(context).getName()",
                (TestSuiteDescriptor d) -> d.setUpFirstTime(context).getName())
            ).equalTo("BEFORE ALL").$(),
            asString(toPrintable(
                () -> "setUp(context, 0).getName()",
                (TestSuiteDescriptor d) -> d.setUp(context, 0).getName())
            ).equalTo("BEFORE:UtTsDescCase[00]").$(),
            asString(toPrintable(
                () -> "test(context, 0).getName()",
                (TestSuiteDescriptor d) -> d.test(context, 0, 0).getName())
            ).equalTo("TEST:UtTsDescOracle[00].UtTsDescCase[00]").$(),
            asString(toPrintable(
                () -> "tearDoen(context, 0).getName()",
                (TestSuiteDescriptor d) -> d.tearDown(context, 0).getName())
            ).equalTo("AFTER:UtTsDescCase[00]").$(),
            asString(toPrintable(
                () -> "tearDownLastTime(context).getName()",
                (TestSuiteDescriptor d) -> d.tearDownLastTime(context).getName())
            ).equalTo("AFTER ALL").$()
        )
    );
  }
}
