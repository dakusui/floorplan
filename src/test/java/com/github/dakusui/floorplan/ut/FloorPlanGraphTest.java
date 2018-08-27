package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.exception.IncompatibleProfile;
import com.github.dakusui.floorplan.exception.MissingValueException;
import com.github.dakusui.floorplan.exception.TypeMismatch;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.ut.components.ReferenceComponent;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.components.ReferenceComponent.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE;
import static com.github.dakusui.floorplan.utils.FloorPlanUtils.buildFixture;
import static java.util.Collections.singletonList;


public class FloorPlanGraphTest extends UtBase {
  @Test
  public void givenSimpleAttribute$whenConfiguredWithImmediate$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");

    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .configure(
                simple1,
                SimpleComponent.INSTANCE_NAME,
                immediate("configured-instance-name-simple1"))
            .build()
    );

    assertThat(
        fixture.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent> c) -> c.valueOf(SimpleComponent.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.INSTANCE_NAME).equalTo("configured-instance-name-simple1").$(),
            asString("valueOf", SimpleComponent.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("configured-instance-name-simple1").$()
        )
    );
  }

  @Test
  public void givenSimpleAttribute$whenConfiguredWithProfileAttribute$thenResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");

    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .configure(simple1, SimpleComponent.INSTANCE_NAME, profileValue(String.class, "configured-instance-name-simple1"))
            .build()
    );

    assertThat(
        fixture.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent> c) -> c.valueOf(SimpleComponent.INSTANCE_NAME))
                .isInstanceOf(String.class)
                .$(),
            asString("valueOf", SimpleComponent.INSTANCE_NAME)
                .equalTo("profile(configured-instance-name-simple1)")
                .$(),
            asString("valueOf", SimpleComponent.DEFAULT_TO_IMMEDIATE)
                .equalTo("default-value")
                .$(),
            asString("valueOf", SimpleComponent.DEFAULT_TO_INTERNAL_REFERENCE)
                .equalTo("profile(configured-instance-name-simple1)")
                .$()
        )
    );
  }

  @Test
  public void givenSimpleAttribute$whenConfiguredWithSlotAttribute$thenResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");

    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .configure(
                simple1,
                SimpleComponent.INSTANCE_NAME,
                slotValue(String.class, "configured-instance-name-simple1"))
            .build()
    );

    assertThat(
        fixture.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent> c) -> c.valueOf(SimpleComponent.INSTANCE_NAME))
                .isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.INSTANCE_NAME)
                .equalTo("slot(SimpleComponent#simple1, configured-instance-name-simple1)").$(),
            asString("valueOf", SimpleComponent.DEFAULT_TO_IMMEDIATE)
                .equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.DEFAULT_TO_INTERNAL_REFERENCE)
                .equalTo("slot(SimpleComponent#simple1, configured-instance-name-simple1)").$())
    );
  }

  @Test
  public void givenReferencingAttribute$whenConfiguredWithReference$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");
    Policy policy = UtUtils.buildPolicy(
        UtUtils.createUtFloorPlanGraph().add(simple1).add(ref1.spec(), ref1.id()),
        SimpleComponent.SPEC,
        ReferenceComponent.SPEC
    );

    FixtureConfigurator fixtureConfigurator = policy.fixtureConfigurator()
        .configure(
            simple1,
            SimpleComponent.INSTANCE_NAME,
            immediate("configured-instance-name-simple1"))
        .configure(
            ref1,
            REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE,
            referenceTo(simple1));

    assertThat(
        fixtureConfigurator.lookUp(ref1),
        allOf(
            asObject(
                call("resolverFor", REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE)
                    .andThen("get")
                    .andThen("apply", fixtureConfigurator.lookUp(ref1), policy)
                    .$()
            ).isInstanceOf(
                Ref.class
            ).$()
        )
    );
  }

  @Test
  public void givenReferencingAttribute$whenBuilt$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");

    Fixture fixture = buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .add(simple1, ref1)
            .configureWithValue(simple1, SimpleComponent.INSTANCE_NAME, "configured-instance-name-simple1")
            .configure(ref1, REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE, referenceTo(simple1))
            .build()
    );

    assertThat(
        fixture,
        allOf(
            asObject(
                call("lookUp", ref1).andThen("valueOf", REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE).$()
            ).isInstanceOf(
                Component.class
            ).equalTo(
                fixture.lookUp(simple1)
            ).$()
        )
    );
  }

  @Test
  public void givenReferencingAttribute$whenConfiguredWithReferenceAndBuilt$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");
    Fixture fixture = FloorPlanUtils.buildFixture(
        new FixtureDescriptor.Builder(new SimpleProfile())
            .configure(
                simple1,
                SimpleComponent.INSTANCE_NAME,
                immediate("configured-instance-name-simple1"))
            .configure(
                ref1,
                REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE,
                referenceTo(simple1))
            .build());

    assertThat(
        fixture.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Component.class).$(),
            asString(
                "valueOf", ReferenceComponent.REFERENCE_TO_ATTRIBUTE
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

    Fixture fixture = buildFixture(new FixtureDescriptor.Builder(new SimpleProfile())
        .wire(
            singletonList(ref1),
            REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE,
            singletonList(simple1))
        .configure(
            simple1,
            SimpleComponent.INSTANCE_NAME,
            immediate("configured-instance-name-simple1"))
        .build());

    assertThat(
        fixture.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Component.class).$(),
            asString(
                "valueOf", ReferenceComponent.REFERENCE_TO_ATTRIBUTE
            ).equalTo(
                "configured-instance-name-simple1"
            ).$()
        )
    );
  }

  @Test
  public void givenReferencingAttribute2$whenFloorPlanIsConfigured$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");

    Fixture fixture = buildFixture(new FixtureDescriptor.Builder(new SimpleProfile())
        .wire(
            ref1,
            REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE,
            simple1)
        .configure(
            simple1,
            SimpleComponent.INSTANCE_NAME,
            immediate("configured-instance-name-simple1"))
        .build());

    assertThat(
        fixture.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Component.class).$(),
            asString(
                "valueOf", ReferenceComponent.REFERENCE_TO_ATTRIBUTE
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
      UtUtils.buildPolicy(UtUtils.createUtFloorPlanGraph().add(simple1)/*, SimpleComponent.SPEC*/);
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
          UtUtils.createUtFloorPlanGraph().add(simple1),
          SimpleComponent.SPEC
      ).fixtureConfigurator().build();
    } catch (MissingValueException e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("Missing value")
              .containsString(SimpleComponent.INSTANCE_NAME.name())
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
      Fixture fixture = UtUtils.buildPolicy(UtUtils.createUtFloorPlanGraph().add(simple1), SimpleComponent.SPEC).fixtureConfigurator()
          .configure(simple1, SimpleComponent.INSTANCE_NAME, immediate(123))
          .build();
      System.out.println(String.format("value='%s'", fixture.lookUp(simple1).valueOf(SimpleComponent.INSTANCE_NAME)));
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
        UtUtils.createUtFloorPlanGraph()
            .add(simple1)
            .requireProfile(p -> false), // Give a requirement that is never met.
        SimpleComponent.SPEC));
  }
}
