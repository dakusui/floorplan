package com.github.dakusui.floorplan.policy;

import com.github.dakusui.floorplan.Fixture;
import com.github.dakusui.floorplan.FixtureConfigurator;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;

import java.util.*;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.*;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Policy<F extends Fixture> {
  <A extends Attribute, T> Resolver<A, T> fallbackResolverFor(Ref ref, A attr);

  Profile profile();

  /**
   * Returns a {@code FixtureConfigurator} instance.
   */
  FixtureConfigurator<F> fixtureConfigurator();

  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  class Impl<F extends Fixture> implements Policy<F> {
    private final FixtureConfigurator<F> fixtureConfigurator;
    private final Profile                profile;

    Impl(List<ResolverEntry> resolvers, Collection<ComponentSpec<?>> specs, FloorPlan<F> floorPlan, Profile profile, Fixture.Factory<F> fixtureFactory) {
      requireArgument(
          floorPlan,
          f -> f.allReferences().stream().allMatch(ref -> specs.contains(ref.spec())),
          () -> String.format(
              "References using unknown specs are found.: %s",
              floorPlan.allReferences().stream().filter(ref -> !specs.contains(ref.spec())).collect(toList())
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
    public FixtureConfigurator<F> fixtureConfigurator() {
      return this.fixtureConfigurator;
    }

    @Override
    public <A extends Attribute> Configurator<A> lookUp(Ref ref) {
      return this.fixtureConfigurator().lookUp(ref);
    }

  }

  class Builder<F extends Fixture> {
    private final List<ResolverEntry>    resolvers      = new LinkedList<>();
    private final List<ComponentSpec<?>> specs          = new LinkedList<>();
    private       FloorPlan              floorPlan      = null;
    private       Profile                profile;
    @SuppressWarnings("unchecked")
    private       Fixture.Factory<?>     fixtureFactory =
        (policy, fixtureConfigurator) -> (F) new Fixture.Base(policy, fixtureConfigurator) {
        };

    public Builder() {
    }

    public Builder<F> setFloorPlan(FloorPlan<F> floorPlan) {
      requireState(this, v -> v.floorPlan == null);
      this.resolvers.addAll(createResolversForFloorPlan(floorPlan));
      this.floorPlan = requireNonNull(floorPlan);
      return this;
    }

    public Builder<F> setProfile(Profile profile) {
      this.profile = profile;
      return this;
    }

    public Builder<F> addComponentSpec(ComponentSpec<?> spec) {
      requireNonNull(spec);
      this.resolvers.addAll(createResolversForComponentSpec(spec));
      this.specs.add(spec);
      return this;
    }

    public Builder<F> setFixtureFactory(Fixture.Factory<F> fixtureFactory) {
      this.fixtureFactory = requireNonNull(fixtureFactory);
      return this;
    }

    @SuppressWarnings("unchecked")
    public Policy<F> build() {
      require(
          requireNonNull(this.profile),
          p -> requireNonNull(this.floorPlan).canBeDeployedOn(p),
          Exceptions.incompatibleProfile(floorPlan, profile)
      );
      return new Impl<>(new LinkedList<ResolverEntry>(resolvers) {{
        reverse(this);
      }}, this.specs,
          this.floorPlan,
          this.profile,
          requireNonNull(this.fixtureFactory));
    }

    private static <F extends Fixture> List<? extends ResolverEntry> createResolversForFloorPlan(FloorPlan<F> floorPlan) {
      return floorPlan.allWires();
    }

    private static List<ResolverEntry> createResolversForComponentSpec(ComponentSpec<?> spec) {
      return new LinkedList<ResolverEntry>() {{
        Arrays.stream(
            spec.attributes()
        ).map(
            attribute -> new ResolverEntry(
                (ref, a) -> Objects.equals(attribute.spec(), ref.spec()) && Objects.equals(attribute, a),
                attribute.defaultValueResolver()
            )
        ).forEach(
            this::add
        );
      }};
    }
  }
}
