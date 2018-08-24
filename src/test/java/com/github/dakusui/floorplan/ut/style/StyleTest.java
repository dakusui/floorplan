package com.github.dakusui.floorplan.ut.style;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import com.github.dakusui.floorplan.ut.style.models.*;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import org.junit.Test;

import java.util.NoSuchElementException;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.utils.FloorPlanUtils.buildFixture;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

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

    ClassStyle<ClassStyle.Attr> sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    assertThat(
        sandbox,
        asString("name").equalTo("helloWorld").$()
    );
  }

  @Test
  public void inheritedClassStyle() {
    Ref sandboxRef = Ref.ref(InheritedClassStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, ClassStyle.Attr.NAME, immediate("helloWorld"))
            .build()
    );

    InheritedClassStyle sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    System.out.println(sandbox.url());
    assertThat(
        sandbox,
        allOf(
            asString("name").equalTo("<helloWorld>").$(),
            asString("url").equalTo("http://localhost:8081/helloWorld").$()
        ));
  }

  @Test(expected = NoSuchElementException.class)
  public void brokenModel() {
    try {
      Ref sandboxRef = Ref.ref(BrokenModel.SPEC, "1");
      buildFixture(
          new FixtureDescriptor.Builder(new SimpleProfile())
              .add(sandboxRef)
              .configure(sandboxRef, BrokenModel.Attr.NAME, immediate("helloWorld"))
              .build()
      );
    } catch (NoSuchElementException e) {
      assertThat(
          e.getMessage(),
          asString().containsString("Fallback resolver for").containsString("was not found").$()
      );
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void illegalArgument() {
    try {
      Ref sandboxRef = Ref.ref(ClassStyle.SPEC, "1");
      buildFixture(
          new FixtureDescriptor.Builder(new SimpleProfile())
              .add(sandboxRef)
              .configure(sandboxRef, InterfaceStyle.Attr.NAME, immediate("helloWorld"))
              .build()
      );
    } catch (IllegalArgumentException e) {
      assertThat(
          e.getMessage(),
          asString().containsString("not compatible with").$()
      );
      throw e;
    }
  }


  @Test
  public void interfaceStyle() {
    Ref sandboxRef = Ref.ref(InterfaceStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, InterfaceStyle.Attr.NAME, immediate("helloWorld"))
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
  public void inheritedInterfaceStyle() {
    InterfaceStyle.SPEC.attributes().forEach(System.out::println);
    InheritedInterfaceStyle.SPEC.attributes().forEach(System.out::println);
    Ref sandboxRef = Ref.ref(InheritedInterfaceStyle.SPEC, "1");
    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(sandboxRef)
            .configure(sandboxRef, InterfaceStyle.Attr.NAME, immediate("helloWorld"))
            .build()
    );

    InheritedInterfaceStyle sandbox = fixture.lookUp(sandboxRef);
    System.out.println(sandbox.getClass().getCanonicalName());
    System.out.println(sandbox.name());
    System.out.println(sandbox.url());
    assertThat(
        sandbox,
        allOf(
            asString("name").equalTo("{helloWorld}").$(),
            asString("url").equalTo("http://localhost:8080/helloWorld").$()
        ));
  }

  @Test
  public void parentAttribute() {
    ClassStyle.SPEC.attributes().forEach(System.out::println);
    assertThat(
        ClassStyle.SPEC.attributes(),
        asListOf(Attribute.class).containsExactly(
            singletonList(ClassStyle.Attr.NAME)
        ).$());
  }

  @Test
  public void inheritedAttribute() {
    InheritedClassStyle.SPEC.attributes().forEach(System.out::println);
    assertThat(
        InheritedClassStyle.SPEC.attributes(),
        asListOf(Attribute.class).containsExactly(
            asList(
                InheritedClassStyle.Attr.NAME,
                InheritedClassStyle.Attr.URL
            )).$());

  }
}
