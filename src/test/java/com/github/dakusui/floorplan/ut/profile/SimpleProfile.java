package com.github.dakusui.floorplan.ut.profile;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.policy.Slot;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.HashMap;
import java.util.Map;

public class SimpleProfile implements Profile {
  private final Map<Ref, Slot> slots = new HashMap<>();

  @Override
  public Slot slotFor(Ref ref) {
    return slots.computeIfAbsent(ref, r1 -> new Slot() {
      @SuppressWarnings("unchecked")
      @Override
      public <A extends Attribute, T> Resolver<A, T> resolverFor(Class<T> requestedType, String key) {
        return Resolver.of(c -> p -> (T) (String.format("slot(%s, %s)", r1, key)));
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Attribute, T> Resolver<A, T> resolverFor(String key) {
    return Resolver.of(c -> p ->
        (T) (String.format("profile(%s)", key)));
  }

  @Override
  public <A extends Attribute, T> Resolver<A, T> resolverFor(Class<T> requestedType, String key) {
    if (requestedType.isAssignableFrom(String.class))
      return resolverFor(key);
    return Resolver.of(c -> p -> {
      throw new UnsupportedOperationException();
    });
  }
}
