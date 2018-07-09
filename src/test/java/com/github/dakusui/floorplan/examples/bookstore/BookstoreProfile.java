package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.examples.bookstore.components.Apache;
import com.github.dakusui.floorplan.examples.bookstore.components.PostgreSQL;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.policy.Slot;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.Resolvers;

public class BookstoreProfile implements Profile {
  enum Category {
    LOCAL,
    DEV,
    STG,
    PROD
  }

  private final Category category = Category.LOCAL;

  public Category getCategory() {
    return this.category;
  }

  @Override
  public Slot slotFor(Ref ref) {
    if (ref.spec() == Apache.SPEC)
      return new Slot() {
        @SuppressWarnings("unchecked")
        @Override
        public <A extends Attribute, T> Resolver<A, T> resolverFor(String key) {
          if ("hostname".equals(key))
            return Resolvers.immediate((T) "webserver.localdomain");
          if ("port".equals(key))
            return Resolvers.immediate((T) Integer.valueOf(80));
          throw new RuntimeException("Unknown key:" + key);
        }
      };
    if (ref.spec() == PostgreSQL.SPEC)
      return new Slot() {
        @SuppressWarnings("unchecked")
        @Override
        public <A extends Attribute, T> Resolver<A, T> resolverFor(String key) {
          if ("hostname".equals(key))
            return Resolvers.immediate((T) "dbserver.localdomain");
          if ("port".equals(key))
            return Resolvers.immediate((T) Integer.valueOf(5432));
          throw new RuntimeException("Unknown key:" + key);
        }
      };
    throw new RuntimeException("No slot for this component spec:" + ref);
  }

  @Override
  public <A extends Attribute, T> Resolver<A, T> resolverFor(String key) {
    throw new RuntimeException("Unknown key:" + key);
  }
}
