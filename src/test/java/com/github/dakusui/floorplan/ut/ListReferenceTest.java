package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class ListReferenceTest {
  /**
   * Component under test
   */
  public static class Cut {
    enum Attr implements Attribute {
      LIST_REF_ATTR(SPEC.property(List.class).defaultsTo(nothing()).$());

      final private Bean<Attr> bean;

      Attr(Bean<Attr> bean) {
        this.bean = bean;
      }

      @SuppressWarnings("unchecked")
      @Override
      public Bean<Attr> bean() {
        return bean;
      }
    }

    public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
  }

  @Test
  public void givenListAttribute$whenConfiguredThroughFloorPlan$thenResolvedToCorrectValue() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "1");
    Ref simple2 = Ref.ref(SimpleComponent.SPEC, "2");
    Ref cut = Ref.ref(Cut.SPEC, "1");

    Fixture fixture = UtUtils.buildPolicy(
        UtUtils.createUtFloorPlan()
            .add(simple1, simple2, cut)
            .wire(cut, Cut.Attr.LIST_REF_ATTR, simple1, simple2),
        Cut.SPEC,
        SimpleComponent.SPEC
    ).fixtureConfigurator(
    ).configure(simple1, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins01")
    ).configure(simple2, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins02")
    ).build();

    assertThat(
        fixture.lookUp(cut).<Component>valueOf(Cut.Attr.LIST_REF_ATTR, 0),
        asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("ins01").$()
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenListAttribute$whenConfiguredThroughConfigurator$thenResolvedToCorrectValue() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "1");
    Ref simple2 = Ref.ref(SimpleComponent.SPEC, "2");
    Ref cut = Ref.ref(Cut.SPEC, "1");

    Fixture fixture = UtUtils.buildPolicy(
        UtUtils.createUtFloorPlan()
            .add(simple1, simple2, cut),
        Cut.SPEC,
        SimpleComponent.SPEC
    ).fixtureConfigurator(
    ).configure(cut, Cut.Attr.LIST_REF_ATTR, listOf(Ref.class, referenceTo(simple1), referenceTo(simple2))
    ).configure(simple1, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins01")
    ).configure(simple2, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins02")
    ).build();

    assertThat(
        fixture.lookUp(cut).<Component<SimpleComponent.Attr>>valueOf(Cut.Attr.LIST_REF_ATTR, 1),
        asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).eq("ins02").$()
    );
  }
}
