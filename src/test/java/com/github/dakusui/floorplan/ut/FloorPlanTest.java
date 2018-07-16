package com.github.dakusui.floorplan.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Operator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.exception.IncompatibleProfile;
import com.github.dakusui.floorplan.exception.MissingValueException;
import com.github.dakusui.floorplan.exception.TypeMismatch;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.ut.components.ReferenceComponent;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class FloorPlanTest {
  @Test
  public void givenSimpleAttribute$whenConfiguredWithImmediate$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1), SimpleComponent.SPEC);

    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).build();

    assertThat(
        fixture.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent.Attr> c) -> c.valueOf(SimpleComponent.Attr.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("configured-instance-name-simple1").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("configured-instance-name-simple1").$()
        )
    );
  }

  @Test
  public void givenSimpleAttribute$whenConfiguredWithProfileAttribute$thenResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1), SimpleComponent.SPEC);

    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        profileValue("configured-instance-name-simple1")
    ).build();

    assertThat(
        fixture.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent.Attr> c) -> c.valueOf(SimpleComponent.Attr.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("profile(configured-instance-name-simple1)").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("profile(configured-instance-name-simple1)").$()
        )
    );
  }

  @Test
  public void givenSimpleAttribute$whenConfiguredWithSlotAttribute$thenResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1), SimpleComponent.SPEC);

    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        slotValue("configured-instance-name-simple1")
    ).build();

    assertThat(
        fixture.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent.Attr> c) -> c.valueOf(SimpleComponent.Attr.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("slot(SimpleComponent#simple1, configured-instance-name-simple1)").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("slot(SimpleComponent#simple1, configured-instance-name-simple1)").$()
        )
    );
  }

  @Test
  public void givenSimpleComponent$whenConfigureInstaller$thenIntendedOperatorIsUsed() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1), SimpleComponent.SPEC);

    List<String> out = new LinkedList<>();
    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).addOperatorFactory(
        simple1,
        Operator.Factory.of(
            Operator.Type.INSTALL,
            c -> context -> context.simple("simple", () -> out.add("hello"))
        )
    ).build();

    new ReportingActionPerformer.Builder(
        fixture.lookUp(simple1).install().apply(new Context.Impl())
    ).build(
    ).performAndReport();

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(1).$(),
            asString("get", 0).equalTo("hello").$()
        )
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenSimpleComponent$whenInstallerIsNotConfigured$thenExceptionThrown() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1), SimpleComponent.SPEC);

    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).build();

    new ReportingActionPerformer.Builder(
        fixture.lookUp(simple1).install().apply(new Context.Impl())
    ).build(
    ).performAndReport();
  }


  @Test
  public void givenReferencingAttribute$whenConfiguredWithReference$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");
    Policy policy = UtUtils.buildPolicy(
        UtUtils.createUtFloorPlan().add(simple1).add(ref1.spec(), ref1.id()),
        SimpleComponent.SPEC,
        ReferenceComponent.SPEC
    );

    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).configure(
        ref1,
        ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE,
        referenceTo(simple1)
    ).build();

    assertThat(
        fixture.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Component.class).$(),
            asString(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ATTRIBUTE
            ).equalTo(
                "configured-instance-name-simple1"
            ).$()
        )
    );
  }

  @Test
  public void givenReferencingAttribute$whenFloorPlanIsConfigured$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");
    Policy policy = UtUtils.buildPolicy(
        UtUtils.createUtFloorPlan().add(
            simple1
        ).add(
            ref1
        ).wire(
            ref1, ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE, simple1
        ),
        SimpleComponent.SPEC,
        ReferenceComponent.SPEC
    );

    Fixture fixture = policy.fixtureConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).build();

    assertThat(
        fixture.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Component.class).$(),
            asString(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ATTRIBUTE
            ).equalTo(
                "configured-instance-name-simple1"
            ).$()
        )
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownSpec() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    try {
      UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1)/*, SimpleComponent.SPEC*/);
    } catch (IllegalArgumentException e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("References using unknown specs")
              .containsString("SimpleComponent#simple1")
              .$()
      );
      throw e;
    }
  }

  @Test(expected = MissingValueException.class)
  public void missingValue() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    try {
      UtUtils.buildPolicy(
          UtUtils.createUtFloorPlan().add(simple1),
          SimpleComponent.SPEC
      ).fixtureConfigurator().build();
    } catch (MissingValueException e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("Missing value")
              .containsString(SimpleComponent.Attr.INSTANCE_NAME.name())
              .containsString(simple1.toString())
              .$()
      );
      throw e;
    }
  }


  @Test(expected = TypeMismatch.class)
  public void typeMismatch() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    try {
      Fixture fixture = UtUtils.buildPolicy(UtUtils.createUtFloorPlan().add(simple1), SimpleComponent.SPEC).fixtureConfigurator()
          .configure(simple1, SimpleComponent.Attr.INSTANCE_NAME, immediate(123))
          .build();
      System.out.println(String.format("value='%s'", fixture.lookUp(simple1).valueOf(SimpleComponent.Attr.INSTANCE_NAME)));
    } catch (TypeMismatch e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("A value of")
              .containsString(String.class.getSimpleName())
              .containsString("123")
              .containsString(Integer.class.getCanonicalName())
              .$()
      );
      throw e;
    }
  }

  @Test(expected = IncompatibleProfile.class)
  public void givenIncompatibleProfile$whenPolicyIsBuilt$thenExceptionIsThrown() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    System.out.println(UtUtils.buildPolicy(
        UtUtils.createUtFloorPlan()
            .add(simple1)
            .requireProfile(p -> false), // Give a requirement that is never met.
        SimpleComponent.SPEC));
  }
}
