package com.github.dakusui.floorplan.policy;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.resolver.Resolver;

public interface Slot {
  <A extends Attribute, T> Resolver<A, T> resolverFor(Class<T> requestedType, String key);
}
