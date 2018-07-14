package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.exception.InconsistentSpec;
import com.github.dakusui.floorplan.ut.tdesc.UtTsDescFloorPlan;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.buildPolicy;

public class InheritanceTest {
  @Test
  public void testL1() {
    assertThat(
        Attribute.attributes(L1.Attr.class),
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
        Attribute.attributes(L2.Attr.class),
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
        Attribute.attributes(L3.Attr.class),
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
    Fixture fixture = buildPolicy(new UtTsDescFloorPlan().add(cut), L1.SPEC).fixtureConfigurator().build();

    assertThat(
        fixture.lookUp(cut).valueOf(L1.Attr.NAME),
        asString().equalTo("defaultName").$()
    );
  }

  @Test
  public void testL2$whenBuilt() {
    Ref cut = Ref.ref(L2.SPEC, "1");
    Fixture fixture = buildPolicy(new UtTsDescFloorPlan().add(cut), L1.SPEC, L2.SPEC).fixtureConfigurator().build();

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
    Fixture fixture = buildPolicy(new UtTsDescFloorPlan().add(cut), L1.SPEC, L2.SPEC, L3.SPEC).fixtureConfigurator().build();

    assertThat(
        fixture.lookUp(cut),
        allOf(
            asString("valueOf", L3.Attr.NAME).equalTo("defaultName").$(),
            asString("valueOf", L3.Attr.NAME2).equalTo("defaultName").$(),
            asString("valueOf", L3.Attr.NAME3).equalTo("overridden").$()
        )
    );
  }

  @Test(expected = InconsistentSpec.class)
  public void testL3$whenBuilt$thenError() {
    Ref cut = Ref.ref(LE.SPEC, "1");
    buildPolicy(new UtTsDescFloorPlan().add(cut), L1.SPEC, L2.SPEC, L3.SPEC, LE.SPEC).fixtureConfigurator().build();
  }

  public static class L1 {
    public interface Attr extends Attribute {
      Attr NAME = Attribute.create("NAME", SPEC.property(String.class).defaultsTo(immediate("defaultName")).$());
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }

  public static class L2 {
    public interface Attr extends L1.Attr {
      Attr NAME2 = Attribute.create(
          "NAME2",
          Attr.class,
          SPEC.property(String.class).defaultsTo(referenceTo(L1.Attr.NAME)).$()
      );

      Attr NAME3 = Attribute.create(
          "NAME3",
          Attr.class,
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
          "NAME3",
          Attr.class,
          SPEC.property(String.class).defaultsTo(immediate("overridden")).$());
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }

  public static class LE {
    public interface Attr extends L2.Attr {
      Attr NAME3 = Attribute.create(
          "NAMEE",
          Attr.class,
          SPEC.property(String.class).defaultsTo(immediate("overridden")).$());
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }

}
