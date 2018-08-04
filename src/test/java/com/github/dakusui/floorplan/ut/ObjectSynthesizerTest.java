package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.ut.utils.UtBase;
import com.github.dakusui.floorplan.utils.ObjectSynthesizer;
import org.junit.Before;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.ObjectSynthesizer.Default.methodCall;

public class ObjectSynthesizerTest extends UtBase {
  private X fallbackObject;

  interface A {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String aMethod();
  }

  interface B {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String bMethod();
  }

  interface C {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String cMethod();
  }

  interface X extends A, B, C {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String xMethod();
  }

  @Before
  public void before() {
    this.fallbackObject = new X() {
      @Override
      public String xMethod() {
        return "xMethod";
      }

      @Override
      public String cMethod() {
        return "cMethod";
      }

      @Override
      public String bMethod() {
        return "bMethod";
      }

      @Override
      public String aMethod() {
        return "aMethod";
      }
    };
  }

  @Test
  public void whenMethodsCalled$thenProxiedToIntendedMethods() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .synthesize();
    assertThat(
        x,
        allOf(
            asString("aMethod").equalTo("a is called").$(),
            asString("bMethod").equalTo("b is called").$(),
            asString("toString").startsWith("com.github.dakusui.floorplan.ut.ObjectSynthesizerTest$1@").$(),
            asString("cMethod").equalTo("cMethod").$(),
            asString("xMethod").equalTo("xMethod").$(),
            asInteger(call("xMethod").andThen("toString").andThen("length").$()).equalTo(7).$()
        ));
  }

  @Test
  public void whenEqualsOnSameObject$thenTrue() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .synthesize();
    assertThat(
        x,
        allOf(
            asBoolean("equals", x).isTrue().$(),
            asBoolean("equals", fallbackObject).isTrue().$()
        ));
  }

  @Test
  public void whenEqualsOnAnotherObject$thenFalse() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .synthesize();
    assertThat(
        x,
        asBoolean("equals", "Hello").isFalse().$()
    );
  }
}
