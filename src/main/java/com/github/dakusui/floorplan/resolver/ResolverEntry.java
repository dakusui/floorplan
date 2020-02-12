package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;

import java.util.function.BiPredicate;

import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;

public class ResolverEntry {
  public final  BiPredicate<Ref, Attribute> cond;
  public final  Resolver<Attribute, ?>      resolver;
  private final Attribute                   keyAttribute;

  public ResolverEntry(Attribute keyAttribute, BiPredicate<Ref, Attribute> cond, Resolver<Attribute, ?> resolver) {
    this.keyAttribute = requireNonNull(keyAttribute);
    this.cond = cond;
    this.resolver = resolver;
  }

  public Attribute key() {
    return this.keyAttribute;
  }

  public String toString() {
    return String.format("when:%s resolveTo:%s", this.cond, this.resolver);
  }
}
