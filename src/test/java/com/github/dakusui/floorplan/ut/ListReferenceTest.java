package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FloorPlanDescriptor;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import com.github.dakusui.floorplan.ut.profile.SimpleProfile;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.component.Ref.ref;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.utils.FloorPlanUtils.buildFloorPlan;
import static com.github.dakusui.floorplan.utils.InternalUtils.isEqualTo;
import static com.github.dakusui.floorplan.utils.InternalUtils.isInstanceOf;

@RunWith(Enclosed.class)
public class ListReferenceTest {
  public static class Basic {
    /**
     * Component under test
     */
    public static class Cut {
      enum Attr implements Attribute {
        LIST_REF_ATTR(SPEC.property(List.class).required().$());

        final private Definition<Attr> definition;

        Attr(Definition<Attr> definition) {
          this.definition = definition;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Definition<Attr> definition() {
          return definition;
        }
      }

      public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
    }

    @Test
    public void givenListAttribute$whenConfiguredThroughFloorPlan$thenResolvedToCorrectValue() {
      Ref simple1 = ref(SimpleComponent.SPEC, "1");
      Ref simple2 = ref(SimpleComponent.SPEC, "2");
      Ref cut = ref(Cut.SPEC, "1");

      FloorPlan floorPlan = UtUtils.buildPolicy(
          UtUtils.createUtFloorPlanGraph()
              .add(simple1, simple2, cut)
              .wire(cut, Cut.Attr.LIST_REF_ATTR, simple1, simple2),
          Cut.SPEC,
          SimpleComponent.SPEC
      ).floorPlanConfigurator(
      ).configure(simple1, SimpleComponent.INSTANCE_NAME, immediate("ins01")
      ).configure(simple2, SimpleComponent.INSTANCE_NAME, immediate("ins02")
      ).build();

      assertThat(
          floorPlan.lookUp(cut).<Component>valueOf(Cut.Attr.LIST_REF_ATTR, 0),
          asString("valueOf", SimpleComponent.INSTANCE_NAME).equalTo("ins01").$()
      );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenListAttribute$whenConfiguredThroughConfigurator$thenResolvedToCorrectValue() {
      Ref simple1 = ref(SimpleComponent.SPEC, "1");
      Ref simple2 = ref(SimpleComponent.SPEC, "2");
      Ref cut = ref(Cut.SPEC, "1");

      FloorPlan floorPlan = UtUtils.buildPolicy(
          UtUtils.createUtFloorPlanGraph()
              .add(simple1, simple2, cut),
          Cut.SPEC,
          SimpleComponent.SPEC
      ).floorPlanConfigurator(
      ).configure(cut, Cut.Attr.LIST_REF_ATTR, listOf(Ref.class, referenceTo(simple1), referenceTo(simple2))
      ).configure(simple1, SimpleComponent.INSTANCE_NAME, immediate("ins01")
      ).configure(simple2, SimpleComponent.INSTANCE_NAME, immediate("ins02")
      ).build();

      assertThat(
          floorPlan.lookUp(cut).<Component<SimpleComponent>>valueOf(Cut.Attr.LIST_REF_ATTR, 1),
          asString("valueOf", SimpleComponent.INSTANCE_NAME).eq("ins02").$()
      );
    }
  }

  public static class Issue36 {
    public interface Child extends Component<Child.Attr> {
      ComponentSpec<Attr> SPEC = ComponentSpec.create(Child.class, Attr.class);

      interface Attr extends Attribute {
      }
    }

    public interface Parent extends Component<Parent.Attr> {
      ComponentSpec<Attr> SPEC = ComponentSpec.create(Parent.class, Attr.class);

      interface Attr extends Attribute {
        Attr LIST_REF = Attribute.create(SPEC.listPropertyOf(Child.SPEC).required().$());
      }

      default List<Child> children() {
        return this.valueOf(Attr.LIST_REF);
      }
    }

    @Test
    public void given$when$then() {
      Ref refChild = ref(Child.SPEC, "1");
      Ref refParent = ref(Parent.SPEC, "1");
      FloorPlan floorPlan = buildFloorPlan(new FloorPlanDescriptor.Builder(new SimpleProfile())
          .add(refChild)
          .wire(refParent, Parent.Attr.LIST_REF, refChild)
          .build());
      Parent parent = floorPlan.lookUp(refParent);
      assertThat(
          parent.children(),
          allOf(
              asListOf(Child.class).check(call("size").$(), isEqualTo(1)).$(),
              asListOf(Child.class).check(call("get", 0).$(), isInstanceOf(Child.class)).$()
          )
      );
    }
  }
}
