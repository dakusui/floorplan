package com.github.dakusui.floorplan.policy;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.Fixture;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.*;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Policy {
  <A extends Attribute, T> Resolver<A, T> fallbackResolverFor(Ref ref, A attr);

  Profile profile();

  /**
   * Returns a {@code FixtureConfigurator} instance associated with this object.
   *
   * @return  a {@code FixtureConfigurator} instance
   */
  FixtureConfigurator fixtureConfigurator();

  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  class Impl implements Policy {
    private final FixtureConfigurator fixtureConfigurator;
    private final Profile             profile;

    Impl(List<ResolverEntry> resolvers, Collection<ComponentSpec<?>> specs, FloorPlan floorPlan, Profile profile, Fixture.Factory fixtureFactory) {
      requireArgument(
          floorPlan,
          f -> f.allReferences().stream().allMatch((Ref ref) -> specs.contains(ref.spec())),
          () -> String.format(
              "References using unknown specs are found.: %s",
              floorPlan.allReferences().stream().filter((Ref ref) -> !specs.contains(ref.spec())).collect(toList())
          )
      );
      this.resolvers = unmodifiableList(requireNonNull(resolvers));
      this.fixtureConfigurator = requireNonNull(floorPlan).configurator(this, fixtureFactory);
      this.profile = requireNonNull(profile);
    }

    final List<ResolverEntry> resolvers;

    @SuppressWarnings("unchecked")
    public <A extends Attribute, T> Resolver<A, T> fallbackResolverFor(Ref ref, A attr) {
      return resolvers.stream().filter(
          resolverEntry -> resolverEntry.cond.test(ref, attr)
      ).findFirst(
      ).map(
          resolverEntry -> (Resolver<A, T>) resolverEntry.resolver
      ).orElseThrow(
          noSuchElement("Fall back resolver for '%s' of '%s' was not found.", attr, ref)
      );
    }

    @Override
    public Profile profile() {
      return this.profile;
    }

    @Override
    public FixtureConfigurator fixtureConfigurator() {
      return this.fixtureConfigurator;
    }

    @Override
    public <A extends Attribute> Configurator<A> lookUp(Ref ref) {
      return this.fixtureConfigurator().lookUp(ref);
    }

  }

  class Builder {
    private final List<ResolverEntry>    resolvers      = new LinkedList<>();
    private final List<ComponentSpec<?>> specs          = new LinkedList<>();
    private       FloorPlan              floorPlan      = null;
    private       Profile                profile;
    @SuppressWarnings("unchecked")
    private       Fixture.Factory        fixtureFactory =
        Fixture.Impl::new;

    public Builder() {
    }

    public Builder setFloorPlan(FloorPlan floorPlan) {
      requireState(this, v -> v.floorPlan == null);
      this.resolvers.addAll(createResolversForFloorPlan(requireNonNull(floorPlan)));
      this.floorPlan = requireNonNull(floorPlan);
      return this;
    }

    public Builder setProfile(Profile profile) {
      this.profile = profile;
      return this;
    }

    public Builder addComponentSpec(ComponentSpec<?> spec) {
      requireNonNull(spec);
      this.resolvers.addAll(createResolversForComponentSpec(spec));
      this.specs.add(spec);
      return this;
    }

    public Builder setFixtureFactory(Fixture.Factory fixtureFactory) {
      this.fixtureFactory = requireNonNull(fixtureFactory);
      return this;
    }

    @SuppressWarnings("unchecked")
    public Policy build() {
      require(
          requireNonNull(this.profile),
          p -> requireNonNull(this.floorPlan).isCompatibleWith(p),
          Exceptions.incompatibleProfile(floorPlan, profile)
      );
      return new Impl(new LinkedList<ResolverEntry>(resolvers) {{
        reverse(this);
      }}, this.specs,
          this.floorPlan,
          this.profile,
          requireNonNull(this.fixtureFactory));
    }

    private static List<? extends ResolverEntry> createResolversForFloorPlan(FloorPlan floorPlan) {
      return floorPlan.allWires();
    }

    @SuppressWarnings("unchecked")
    private static List<ResolverEntry> createResolversForComponentSpec(ComponentSpec<?> spec) {
      return new LinkedList<ResolverEntry>() {{
        spec.attributes().stream(
        ).map(
            attribute -> new ResolverEntry(
                (ref, a) ->
                    attribute.spec().attributeType().isAssignableFrom(ref.spec().attributeType()) &&
                        Objects.equals(attribute, a),
                (Resolver<Attribute, ?>) attribute.defaultValueResolver()
            )
        ).forEach(
            this::add
        );
      }};
    }
  }
}
