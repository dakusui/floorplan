package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;

public class AttributeTest extends UtBase {
  @Test
  public void testInconsistentMessageSupplier() {
    System.out.println(SimpleComponent.INSTANCE_NAME.name());
    System.out.println(UtComponent.NAME.name());
    assertThat(
        Exceptions.inconsistentSpecMessageSupplier(SimpleComponent.INSTANCE_NAME, UtComponent.NAME).get(),
        allOf(
            asString().containsString(SimpleComponent.INSTANCE_NAME.name()).$(),
            asString().containsString(UtComponent.NAME.name()).$()
        )
    );
  }

  @Test
  public void testAttributeName() {
    assertThat(
        SimpleComponent.INSTANCE_NAME.name(),
        asString().equalTo("INSTANCE_NAME").$()
    );
  }
}
