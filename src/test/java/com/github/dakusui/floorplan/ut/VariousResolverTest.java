package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.ut.floorplan.UtFloorPlan;
import com.github.dakusui.floorplan.resolver.Resolvers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.ut.UtUtils.buildPolicy;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;

@RunWith(Enclosed.class)
public class VariousResolverTest {
  public static class TransformResolver {
    static class Cut {
      enum Attr implements Attribute {
        BASE(SPEC.property(String.class).defaultsTo(immediate("hello")).$()),
        TRANSFORM(SPEC.property(Integer.class).defaultsTo(Resolvers.transform(referenceTo(BASE), String::length)).$());

        private final Bean<Attr> bean;

        Attr(Bean<Attr> bean) {
          this.bean = bean;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Bean<Attr> bean() {
          return this.bean;
        }
      }

      static final ComponentSpec<Cut.Attr> SPEC = new ComponentSpec.Builder<>(Cut.Attr.class).build();
    }

    @Test
    public void givenTransformAttribute$whenEvaluate$thenCorrect() {
      Ref cut = Ref.ref(Cut.SPEC, "1");
      Fixture fixture = buildPolicy(new UtFloorPlan().add(cut), Cut.SPEC).fixtureConfigurator().build();

      assertThat(
          fixture.lookUp(cut).valueOf(Cut.Attr.TRANSFORM),
          asInteger().equalTo(5).$()
      );
    }
  }

  public static class TransformListResolver {
    static class Cut {
      enum Attr implements Attribute {
        @SuppressWarnings("unchecked")
        BASE(SPEC.property(List.class).defaultsTo(listOf(String.class, immediate("hello"), immediate("world!"))).$()),
        TRANSFORM_LIST(SPEC.property(List.class).defaultsTo(Resolvers.transformList(referenceTo(BASE), String::length)).$());

        private final Bean<Attr> bean;

        Attr(Bean<Attr> bean) {
          this.bean = bean;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Bean<Attr> bean() {
          return this.bean;
        }
      }

      static final ComponentSpec<Cut.Attr> SPEC = new ComponentSpec.Builder<>(Cut.Attr.class).build();
    }

    @Test
    public void givenTransformAttribute$whenEvaluate$thenCorrect() {
      Ref cut = Ref.ref(Cut.SPEC, "1");
      Fixture fixture = buildPolicy(new UtFloorPlan().add(cut), Cut.SPEC).fixtureConfigurator().build();

      assertThat(
          fixture.lookUp(cut).valueOf(Cut.Attr.TRANSFORM_LIST),
          allOf(
              asInteger("size").equalTo(2).$(),
              asInteger("get", 0).equalTo(5).$(),
              asInteger("get", 1).equalTo(6).$()
          )
      );
    }
  }

  public static class SizeOfResolver {
    static class Cut {
      enum Attr implements Attribute {
        @SuppressWarnings("unchecked")
        BASE(SPEC.property(List.class).defaultsTo(listOf(String.class, immediate("hello"), immediate("world!"))).$()),
        SIZE_OF(SPEC.property(Integer.class).defaultsTo(Resolvers.sizeOf(referenceTo(BASE))).$());

        private final Bean<Attr> bean;

        Attr(Bean<Attr> bean) {
          this.bean = bean;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Bean<Attr> bean() {
          return this.bean;
        }
      }

      static final ComponentSpec<Cut.Attr> SPEC = new ComponentSpec.Builder<>(Cut.Attr.class).build();
    }

    @Test
    public void givenTransformAttribute$whenEvaluate$thenCorrect() {
      Ref cut = Ref.ref(Cut.SPEC, "1");
      Fixture fixture = buildPolicy(new UtFloorPlan().add(cut), Cut.SPEC).fixtureConfigurator().build();

      assertThat(
          fixture.lookUp(cut).valueOf(Cut.Attr.SIZE_OF),
          asInteger().equalTo(2).$()
      );
    }
  }
}
