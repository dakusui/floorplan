package com.github.dakusui.floorplan.ut.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;

import java.lang.reflect.Field;

public class InterfaceBasedComponent {
  public interface Attr extends Attribute {
    Attr NAME = Attribute.create("NAME", SPEC.property(String.class).$());

  }

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();

  public interface A {
    String NAME = "NAME IN A";
  }

  public interface B extends A {
    String NAME = "NAME IN B";
  }

  public static void main(String[] args) {
    System.out.println(Attr.NAME.valueType());
    System.out.println(Attr.NAME.toString());
    System.out.println(Attr.NAME.name());
    System.out.println(Attribute.attributes(Attr.class).get(0));
    Attr a = Attribute.attributes(Attr.class).get(0);
    System.out.println(a.defaultValueResolver());

    B b = new B() {
    };

    System.out.println("---");
    System.out.println(b.NAME);

    for (Field each : B.class.getFields()) {
      System.out.println(each.getName() + ":" + each.getDeclaringClass());
    }
  }
}

