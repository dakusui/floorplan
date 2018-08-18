package com.github.dakusui.floorplan.ut.style;

import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.floorplan.ut.style.InterfaceStyle.Attr.NAME;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.utils.FloorPlanUtils.buildFixture;

public class StyleTest extends UtBase {
  @Test
  public void classStyle() {
    Ref sandboxRef = Ref.ref(InterfaceStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, NAME, immediate("helloWorld"))
            .build()
    );

    InterfaceStyle sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    assertThat(
        sandbox,
        asString("name").equalTo("helloWorld").$()
    );
  }

  @Test
  public void interfaceStyle() {
    Ref sandboxRef = Ref.ref(InterfaceStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, NAME, immediate("helloWorld"))
            .build()
    );

    InterfaceStyle sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    assertThat(
        sandbox,
        asString("name").equalTo("helloWorld").$()
    );
  }
}
