package com.github.dakusui.floorplan.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.*;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.Utils.newContext;

public class ComponentTest {
  @Test
  public void testActions() {
    Context c = newContext();
    assertThat(
        createComponent(),
        allOf(
            asInteger(
                (Component component) ->
                    component.start().hashCode() + component.stop().hashCode() + component.nuke().hashCode()
            ).$(),
            asString(
                call("start")
                    .andThen("apply", c)
                    .andThen("getName")
                    .$())
                .equalTo("name=START")
                .$(),
            asString(
                call("stop")
                    .andThen("apply", c)
                    .andThen("getName")
                    .$())
                .equalTo("name=STOP")
                .$(),
            asString(
                call("nuke")
                    .andThen("apply", c)
                    .andThen("getName")
                    .$())
                .equalTo("name=NUKE")
                .$()
        )
    );
  }

  private Component createComponent() {
    return new Component() {
      @Override
      public Ref ref() {
        return null;
      }

      @Override
      public ComponentSpec spec() {
        return null;
      }

      @Override
      public ActionFactory actionFactoryFor(Operator.Type op) {
        return c -> c.named("name=" + op.name(), c.nop());
      }

      @Override
      public int sizeOf(Attribute attr) {
        return 0;
      }

      @Override
      public Attribute valueOf(Attribute attr, int index) {
        return null;
      }

      @Override
      public Attribute valueOf(Attribute attr) {
        return null;
      }
    };
  }


}
