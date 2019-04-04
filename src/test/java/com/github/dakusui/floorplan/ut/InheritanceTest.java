package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FloorPlanConfigurator;
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
    FloorPlan floorPlan = buildPolicy(UtUtils.createUtFloorPlanGraph().add(cut), L1.SPEC).floorPlanConfigurator().build();

    assertThat(
        floorPlan.lookUp(cut).valueOf(L1.Attr.NAME),
        asString().equalTo("defaultName").$()
    );
  }

  @Test
  public void testL2$whenBuilt() {
    Ref cut = Ref.ref(L2.SPEC, "1");
    FloorPlan floorPlan = buildPolicy(UtUtils.createUtFloorPlanGraph().add(cut), L1.SPEC, L2.SPEC).floorPlanConfigurator().build();

    assertThat(
        floorPlan.lookUp(cut),
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
    FloorPlan floorPlan = buildPolicy(UtUtils.createUtFloorPlanGraph().add(cut), L1.SPEC, L2.SPEC, L3.SPEC).floorPlanConfigurator().build();

    assertThat(
        floorPlan.lookUp(cut),
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
    FloorPlanConfigurator floorPlanConfigurator = buildPolicy(
        UtUtils.createUtFloorPlanGraph().add(cut),
        L1.SPEC, L2.SPEC, L3.SPEC
    ).floorPlanConfigurator();
    floorPlanConfigurator.lookUp(cut)
        .configure(L3.Attr.NAME, Resolver.of(c -> p -> "configured-1"))
        .configure(L3.Attr.NAME2, Resolver.of(c -> p -> "configured-2"))
        .configure(L3.Attr.NAME3, Resolver.of(c -> p -> "configured-3"));
    FloorPlan floorPlan = floorPlanConfigurator.build();

    L3 l3 = floorPlan.lookUp(cut);
    System.out.println("name1:" + l3.valueOf(L3.Attr.NAME));
    System.out.println("name2:" + l3.valueOf(L3.Attr.NAME2));
    System.out.println("name3:" + l3.valueOf(L3.Attr.NAME3));
    assertThat(
        l3,
        allOf(
            asString("valueOf", L3.Attr.NAME).equalTo("configured-1").$(),
            asString("valueOf", L3.Attr.NAME2).equalTo("configured-2").$(),
            asString("valueOf", L3.Attr.NAME3).equalTo("configured-3").$()
        )
    );
  }

  public interface L1 extends Component<L3.Attr> {
    interface Attr extends Attribute {
      Attr NAME = Attribute.create(SPEC.property(String.class).defaultsTo(immediate("defaultName")).$());
    }


    ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).componentType((Class<? extends Component<Attr>>) L1.class).build();
  }

  public interface L2 extends L1 {
    interface Attr extends L1.Attr {
      Attr NAME2 = Attribute.create(
          SPEC.property(String.class).defaultsTo(referenceTo(L1.Attr.NAME)).$()
      );

      Attr NAME3 = Attribute.create(
          SPEC.property(String.class).defaultsTo(transform(
              referenceTo(L1.Attr.NAME),
              mapper(v -> v + ":" + v))).$()
      );
    }

    ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).componentType((Class<? extends Component<Attr>>) L2.class).build();
  }

  public interface L3 extends L2, L1 {
    interface Attr extends L2.Attr {
      Attr NAME3 = Attribute.create(
          SPEC.property(String.class).defaultsTo(immediate("overridden")).$());
    }

    default <T> T valueOf(L1.Attr l2attr) {
      return null;
    }

    default <T> T valueOf(L2.Attr l2attr) {
      return null;
    }


    ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).componentType(L3.class).build();
  }
}
