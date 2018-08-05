package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class AttributeTest {
  @Test
  public void testInconsistentMessageSupplier() {
    assertThat(
        Exceptions.inconsistentSpecMessageSupplier(SimpleComponent.Attr.INSTANCE_NAME, UtComponent.Attr.NAME).get(),
        allOf(
            asString().containsString(SimpleComponent.Attr.INSTANCE_NAME.name()).$(),
            asString().containsString(UtComponent.Attr.NAME.name()).$()
        )
    );
  }
}
