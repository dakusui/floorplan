package com.github.dakusui.floorplan.policy;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.resolver.Resolver;

/**
 * This interface represents a physical environment where an SUT is deployed.
 * It is expected for a test code to work in the same way even if another profile
 * object is given as long as those profiles are compatible.
 */
public interface Profile {
  Slot slotFor(Ref ref);

  <A extends Attribute, T> Resolver<A, T> resolverFor(String key);

  <A extends Attribute, T> Resolver<A, T> resolverFor(Class<T> requestedType, String key);

  interface Factory<P extends Profile> {
    P create();
  }
}
