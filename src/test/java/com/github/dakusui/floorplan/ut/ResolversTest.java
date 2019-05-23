package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.resolver.Mapper;
import com.github.dakusui.floorplan.resolver.Resolvers;
import com.github.dakusui.floorplan.ut.components.SimpleComponent;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
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
    System.out.println(SimpleComponent.INSTANCE_NAME);
    assertThat(
        Resolvers.referenceTo(SimpleComponent.INSTANCE_NAME).toString(),
        allOf(
            asString().startsWith("referenceTo(attr:").$(),
            asString().containsString(SimpleComponent.INSTANCE_NAME.name()).$()
        ));
  }

  @Test
  public void givenInstanceIdResolver$whenToString$thenCorrectMessageReturned() {
    assertThat(
        Resolvers.instanceId(),
        asString().equalTo("instanceId()").$()
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenInstanceIdResolver$whenApplied$thenCorrectResultReturned() {
    assertThat(
        Resolvers.instanceId().<Attribute>apply(Configurator.class.cast(SimpleComponent.SPEC.configurator("2"))).apply(null),
        asString().equalTo("2").$()
    );
  }

  @Test
  public void givenSlotValueResolver$whenToString$thenCorrectMessageReturned() {
    assertThat(
        Resolvers.slotValue(String.class, "key").toString(),
        asString().equalTo("slotValueOf(String,key)").$()
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenListOfValuesResolver$whenToString$thenCorrectMessageReturned() {
    assertThat(
        Resolvers.listOf(String.class, Resolvers.immediate("key")).toString(),
        asString().equalTo("listOf(String, immediate(key))").$()
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void givenTransformResolver$whenToString$thenCorrectMessageReturned() {
    assertThat(
        Resolvers.transform(Resolvers.immediate("key"), Mapper.create(t -> String.format("<%s>", t))).toString(),
        asString().equalTo("transform(immediate(key), Mapper(noname))").$()
    );
  }


  @SuppressWarnings("unchecked")
  @Test
  public void givenNothingResolver$whenToString$thenCorrectMessageReturned() {
    assertThat(
        Resolvers.nothing().toString(),
        asString().equalTo("a->nothing").$()
    );
  }
}
