package com.github.dakusui.floorplan.ut.style;

import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.ut.style.InterfaceStyle.Attr.NAME;
import static com.github.dakusui.floorplan.utils.FloorPlanUtils.buildFixture;

public class StyleTest extends UtBase {
  @Test
  public void classStyle() {
    Ref sandboxRef = Ref.ref(ClassStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, ClassStyle.Attr.NAME, immediate("helloWorld"))
            .build()
    );

    ClassStyle sandbox = fixture.lookUp(sandboxRef);
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

    InterfaceStyle<InterfaceStyle.Attr> sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    assertThat(
        sandbox,
        asString("name").equalTo("helloWorld").$()
    );
  }

  @Test
  public void inheritedInterfaceStyle() throws NoSuchMethodException {
    Ref sandboxRef = Ref.ref(InheritedInterfaceStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, NAME, immediate("helloWorld"))
            .build()
    );

    InheritedInterfaceStyle sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    System.out.println(sandbox.url());
    assertThat(
        sandbox,
        allOf(
            asString("name").equalTo("<name>").$(),
            asString("url").equalTo("http://localhost:8080/helloWorld").$()
        ));
  }
}
