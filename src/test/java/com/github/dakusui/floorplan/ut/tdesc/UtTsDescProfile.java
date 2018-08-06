package com.github.dakusui.floorplan.ut.tdesc;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.policy.Slot;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.Resolvers;

public class UtTsDescProfile implements Profile {
  @Override
  public Slot slotFor(Ref ref) {
    return new Slot() {
      @SuppressWarnings("unchecked")
      @Override
      public <A extends Attribute, T> Resolver<A, T> resolverFor(Class<T> requestedType, String key) {
        return Resolvers.immediate((T) String.format("valueFor:%s@%s", ref.toString(), key));
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Attribute, T> Resolver<A, T> resolverFor(String key) {
    return Resolvers.immediate((T) String.format("valurFor:%s", key));
  }

  @Override
  public <A extends Attribute, T> Resolver<A, T> resolverFor(Class<T> requestedType, String key) {
    throw new UnsupportedOperationException();
  }
}
