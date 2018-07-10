package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.UtUtils;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.ut.floorplan.UtFloorPlan;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class ListOfResolverTest {
  /**
   * Component under test
   */
  public static class Cut {
    public enum Attr implements Attribute {
      STR(SPEC.property(String.class).defaultsTo(immediate("hi")).$()),
      @SuppressWarnings("unchecked")
      LIST_ATTR(
          SPEC.property(List.class).defaultsTo(listOf(String.class, immediate("hello"), immediate("world"), referenceTo(STR))).$()
      ),;

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
    Ref cut = Ref.ref(Cut.SPEC, "1");
    Fixture fixture = UtUtils.buildPolicy(new UtFloorPlan().add(cut), Cut.SPEC).fixtureConfigurator().build();

    System.out.println("" + fixture.lookUp(cut).valueOf(Cut.Attr.LIST_ATTR));

  }
}
