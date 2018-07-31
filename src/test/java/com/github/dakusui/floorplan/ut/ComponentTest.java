package com.github.dakusui.floorplan.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.component.*;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.InternalUtils.newContext;

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
                    .andThen("name")
                    .$())
                .equalTo("name=START")
                .$(),
            asString(
                call("stop")
                    .andThen("name")
                    .$())
                .equalTo("name=STOP")
                .$(),
            asString(
                call("nuke")
                    .andThen("name")
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
      public Action actionFactoryFor(Operator.Type op) {
        return named("name=" + op.name(), nop());
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
