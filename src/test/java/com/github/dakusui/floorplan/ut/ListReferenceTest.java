package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.UtUtils;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.components.SimpleComponent;
import org.junit.Test;

import java.util.List;

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
  public void test() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "1");
    Ref simple2 = Ref.ref(SimpleComponent.SPEC, "2");
    Ref cut = Ref.ref(Cut.SPEC, "1");

    Fixture fixture = UtUtils.buildPolicy(
        new FloorPlan()
            .add(simple1, simple2, cut)
            .wire(cut, Cut.Attr.LIST_REF_ATTR, simple1, simple2),
        Cut.SPEC,
        SimpleComponent.SPEC
    ).fixtureConfigurator(
    ).configure(simple1, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins01")
    ).configure(simple2, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins02")
    ).build();

    System.out.printf("value='%s'", fixture.lookUp(cut).<Component>valueOf(Cut.Attr.LIST_REF_ATTR, 0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test2() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "1");
    Ref simple2 = Ref.ref(SimpleComponent.SPEC, "2");
    Ref cut = Ref.ref(Cut.SPEC, "1");

    Fixture fixture = UtUtils.buildPolicy(
        new FloorPlan()
            .add(simple1, simple2, cut),
        Cut.SPEC,
        SimpleComponent.SPEC
    ).fixtureConfigurator(
    ).configure(cut, Cut.Attr.LIST_REF_ATTR, listOf(Ref.class, referenceTo(simple1), referenceTo(simple2))
    ).configure(simple1, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins01")
    ).configure(simple2, SimpleComponent.Attr.INSTANCE_NAME, immediate("ins02")
    ).build();

    System.out.printf("value='%s'%n", fixture.lookUp(cut).<Component<SimpleComponent.Attr>>valueOf(Cut.Attr.LIST_REF_ATTR, 0).<String>valueOf(SimpleComponent.Attr.INSTANCE_NAME));
    System.out.printf("value='%s'%n", fixture.lookUp(cut).<Component<SimpleComponent.Attr>>valueOf(Cut.Attr.LIST_REF_ATTR, 1).<String>valueOf(SimpleComponent.Attr.INSTANCE_NAME));
  }
}
