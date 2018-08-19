package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;

import java.util.function.BiPredicate;

public class ResolverEntry {
  public final BiPredicate<Ref, Attribute> cond;
  public final Resolver<Attribute, ?>      resolver;

  public ResolverEntry(BiPredicate<Ref, Attribute> cond, Resolver<Attribute, ?> resolver) {
    this.cond = cond;
    this.resolver = resolver;
  }

  public String toString() {
    return String.format("when:%s resolveTo:%s", this.cond, this.resolver);
  }
}
