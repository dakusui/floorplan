package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.examples.components.SimpleComponent;
import com.github.dakusui.floorplan.resolver.Resolvers;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.floorplan.component.Ref.ref;

public class ResolversTest {
  @Test
  public void givenImmediateResolver$whenToString$thenAppropriateMessageReturned() {
    assertThat(
        Resolvers.immediate("hello").toString(),
        allOf(
            asString().equalTo("immediate(hello)").$()
        ));
  }

  @Test
  public void givenComponentReferenceResolver$whenToString$thenAppropriateMessageReturned() {
    assertThat(
        Resolvers.referenceTo(ref(SimpleComponent.SPEC, "1")).toString(),
        allOf(
            asString().startsWith("referenceTo(component:").$(),
            asString().containsString(ref(SimpleComponent.SPEC, "1").toString()).$()
        ));
  }

  @Test
  public void givenAttributeReferenceResolver$whenToString$thenAppropriateMessageReturned() {
    assertThat(
        Resolvers.referenceTo(SimpleComponent.Attr.INSTANCE_NAME).toString(),
        allOf(
            asString().startsWith("referenceTo(attr:").$(),
            asString().containsString(SimpleComponent.Attr.INSTANCE_NAME.name()).$()
        ));
  }

}
