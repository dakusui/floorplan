package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.utils.ObjectSynthesizer;
import com.github.dakusui.floorplan.ut.utils.UtBase;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.ObjectSynthesizer.Default.methodCall;

public class ObjectSynthesizerTest extends UtBase {
  interface A {
    String aMethod();
  }

  interface B {
    String bMethod();
  }

  interface C {
    String cMethod();
  }

  interface X extends A, B, C {
    String xMethod();
  }

  @Test
  public void test() {
    Object fallbackObject = new X() {
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
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with(args -> "a is called"))
        .handle(methodCall("bMethod").with(args -> "b is called"))
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
    System.out.println(x.aMethod());
    System.out.println(x.bMethod());
    System.out.println(x.toString());
    System.out.println(x.cMethod());
    System.out.println(x.xMethod());
  }

}
