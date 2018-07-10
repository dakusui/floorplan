package com.github.dakusui.floorplan.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.TestSuiteDescriptor;
import com.github.dakusui.floorplan.ut.tdesc.UtTsDescProfile;
import com.github.dakusui.floorplan.ut.tdesc.UtTsDescriptorBuilder;
import com.github.dakusui.floorplan.utils.Utils;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.toPrintable;

public class TestSuiteDescriptorTest {
  @Test
  public void givenUtTestSuiteDesc$when$then() {
    Context context = Utils.newContext();
    assertThat(
        new UtTsDescriptorBuilder().create(new UtTsDescProfile()),
        allOf(
            asString(toPrintable(() -> "getName", TestSuiteDescriptor::getName)).equalTo("UtTsDesc").$(),
            asInteger("size").equalTo(2).$(),
            asString("getNameFor", 0).equalTo("UtTsDesc[00]").$(),
            asString(toPrintable(
                () -> "setUpFirstTime(context).getName()",
                (TestSuiteDescriptor d) -> d.setUpFirstTime(context).getName())
            ).equalTo("BEFORE ALL").$(),
            asString(toPrintable(
                () -> "setUp(context, 0).getName()",
                (TestSuiteDescriptor d) -> d.setUp(context, 0).getName())
            ).equalTo("BEFORE:UtTsDesc[00]").$(),
            asString(toPrintable(
                () -> "test(context, 0).getName()",
                (TestSuiteDescriptor d) -> d.test(context, 0).getName())
            ).equalTo("TEST:UtTsDesc[00]").$(),
            asString(toPrintable(
                () -> "tearDoen(context, 0).getName()",
                (TestSuiteDescriptor d) -> d.tearDown(context, 0).getName())
            ).equalTo("AFTER:UtTsDesc[00]").$(),
            asString(toPrintable(
                () -> "tearDownLastTime(context).getName()",
                (TestSuiteDescriptor d) -> d.tearDownLastTime(context).getName())
            ).equalTo("AFTER ALL").$()
        )
    );
  }
}