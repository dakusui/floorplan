package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.policy.Slot;
import com.github.dakusui.floorplan.resolver.Resolver;

public class BookstoreProfile implements Profile {
  @Override
  public Slot slotFor(Ref ref) {
    return null;
  }

  @Override
  public <A extends Attribute, T> Resolver<A, T> resolverFor(String key) {
    return null;
  }
}
