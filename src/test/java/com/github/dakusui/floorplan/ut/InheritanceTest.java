package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import com.github.dakusui.floorplan.utils.InternalUtils;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.buildPolicy;

public class InheritanceTest {
  @Test
  public void testL1() {
    assertThat(
        InternalUtils.attributes(L1.Attr.class),
        allOf(
            asInteger("size").equalTo(1).$(),
            asString(
                call("get", 0).andThen("name").$())
                .equalTo("NAME").$()
        ));
  }

  @Test
  public void testL2() {
    assertThat(
        InternalUtils.attributes(L2.Attr.class),
        allOf(
            asInteger("size").equalTo(3).$(),
            asString(
                call("get", 0).andThen("name").$())
                .equalTo("NAME").$(),
            asString(
                call("get", 1).andThen("name").$())
                .equalTo("NAME2").$(),
            asString(
                call("get", 2).andThen("name").$())
                .equalTo("NAME3").$()
        ));
  }

  @Test
  public void testL3() {
    assertThat(
        InternalUtils.attributes(L3.Attr.class),
        allOf(
            asInteger("size").equalTo(3).$(),
            asString(
                call("get", 0).andThen("name").$())
                .equalTo("NAME").$(),
            asString(
                call("get", 1).andThen("name").$())
                .equalTo("NAME2").$(),
            asString(
                call("get", 2).andThen("name").$())
                .equalTo("NAME3").$()
        ));
  }

  @Test
  public void testL1$whenBuilt() {
    Ref cut = Ref.ref(L1.SPEC, "1");
    Fixture fixture = buildPolicy(UtUtils.createUtFloorPlan().add(cut), L1.SPEC).fixtureConfigurator().build();

    assertThat(
        fixture.lookUp(cut).valueOf(L1.Attr.NAME),
        asString().equalTo("defaultName").$()
    );
  }

  @Test
  public void testL2$whenBuilt() {
    Ref cut = Ref.ref(L2.SPEC, "1");
    Fixture fixture = buildPolicy(UtUtils.createUtFloorPlan().add(cut), L1.SPEC, L2.SPEC).fixtureConfigurator().build();

    assertThat(
        fixture.lookUp(cut),
        allOf(
            asString("valueOf", L2.Attr.NAME).equalTo("defaultName").$(),
            asString("valueOf", L2.Attr.NAME2).equalTo("defaultName").$(),
            asString("valueOf", L2.Attr.NAME3).equalTo("defaultName:defaultName").$()
        )
    );
  }

  @Test
  public void testL3$whenBuilt() {
    Ref cut = Ref.ref(L3.SPEC, "1");
    Fixture fixture = buildPolicy(UtUtils.createUtFloorPlan().add(cut), L1.SPEC, L2.SPEC, L3.SPEC).fixtureConfigurator().build();

    assertThat(
        fixture.lookUp(cut),
        allOf(
            asString("valueOf", L3.Attr.NAME).equalTo("defaultName").$(),
            asString("valueOf", L3.Attr.NAME2).equalTo("defaultName").$(),
            asString("valueOf", L3.Attr.NAME3).equalTo("overridden").$()
        )
    );
  }

  @Test
  public void testL3$whenConfiguredAndBuilt() {
    Ref cut = Ref.ref(L3.SPEC, "1");
    FixtureConfigurator fixtureConfigurator = buildPolicy(
        UtUtils.createUtFloorPlan().add(cut),
        L1.SPEC, L2.SPEC, L3.SPEC
    ).fixtureConfigurator();
    fixtureConfigurator.lookUp(cut)
        .configure(L3.Attr.NAME, Resolver.of(c -> p -> "configured-1"))
        .configure(L3.Attr.NAME2, Resolver.of(c -> p -> "configured-2"))
        .configure(L3.Attr.NAME3, Resolver.of(c -> p -> "configured-3"));
    Fixture fixture = fixtureConfigurator.build();

    assertThat(
        fixture.lookUp(cut),
        allOf(
            asString("valueOf", L3.Attr.NAME).equalTo("configured-1").$(),
            asString("valueOf", L3.Attr.NAME2).equalTo("configured-2").$(),
            asString("valueOf", L3.Attr.NAME3).equalTo("configured-3").$()
        )
    );
  }

  public static class L1 {
    public interface Attr extends Attribute {
      Attr NAME = Attribute.create(SPEC.property(String.class).defaultsTo(immediate("defaultName")).$());
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }

  public static class L2 {
    public interface Attr extends L1.Attr {
      Attr NAME2 = Attribute.create(
          SPEC.property(String.class).defaultsTo(referenceTo(L1.Attr.NAME)).$()
      );

      Attr NAME3 = Attribute.create(
          SPEC.property(String.class).defaultsTo(transform(
              referenceTo(L1.Attr.NAME),
              mapper(v -> v + ":" + v))).$()
      );
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }

  public static class L3 {
    public interface Attr extends L2.Attr {
      Attr NAME3 = Attribute.create(
          SPEC.property(String.class).defaultsTo(immediate("overridden")).$());
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }
}
