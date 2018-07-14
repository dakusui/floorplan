package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;

import java.util.function.Function;

public enum Mappers {
  ;

  public static <A extends Attribute, T, R> Mapper<A, T, R> mapper(Function<T, R> func) {
    return Mapper.of(a -> c -> p -> func);
  }
}
