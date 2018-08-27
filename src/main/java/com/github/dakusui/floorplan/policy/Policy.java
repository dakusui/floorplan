package com.github.dakusui.floorplan.policy;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FloorPlanGraph;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.*;
import static com.github.dakusui.floorplan.utils.InternalUtils.printableBiPredicate;
import static com.github.dakusui.floorplan.utils.InternalUtils.shortenedClassName;
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
   * @return a {@code FixtureConfigurator} instance
   */
  FixtureConfigurator fixtureConfigurator();

  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  class Impl implements Policy {
    private final FixtureConfigurator fixtureConfigurator;
    private final Profile             profile;

    Impl(List<ResolverEntry> resolvers, Collection<ComponentSpec<?>> specs, FloorPlanGraph floorPlanGraph, Profile profile, FloorPlan.Factory fixtureFactory) {
      requireArgument(
          floorPlanGraph,
          f -> f.allReferences().stream().allMatch((Ref ref) -> specs.contains(ref.spec())),
          () -> String.format(
              "References using unknown specs are found.: %s",
              floorPlanGraph.allReferences().stream().filter((Ref ref) -> !specs.contains(ref.spec())).collect(toList())
          )
      );
      this.resolvers = unmodifiableList(requireNonNull(resolvers));
      this.fixtureConfigurator = requireNonNull(floorPlanGraph).configurator(this, fixtureFactory);
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
          noSuchElement(
              "Fallback resolver for '%s'(%s) of '%s' was not found. Known resolvers are %s",
              attr, attr.spec(), ref, resolvers));
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
    private       FloorPlanGraph         floorPlanGraph = null;
    private       Profile                profile;
    @SuppressWarnings("unchecked")
    private       FloorPlan.Factory      fixtureFactory = FloorPlan.Impl::new;

    public Builder() {
    }

    public Builder setFloorPlanGraph(FloorPlanGraph floorPlanGraph) {
      requireState(this, v -> v.floorPlanGraph == null);
      this.resolvers.addAll(createResolversForFloorPlan(requireNonNull(floorPlanGraph)));
      this.floorPlanGraph = requireNonNull(floorPlanGraph);
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

    @SuppressWarnings("unchecked")
    public Policy build() {
      Predicate<Profile> req = p -> requireNonNull(this.floorPlanGraph).isCompatibleWith(p);
      require(
          requireNonNull(this.profile),
          req,
          Exceptions.incompatibleProfile(profile, req)
      );
      return new Impl(new LinkedList<ResolverEntry>(resolvers) {{
        reverse(this);
      }}, this.specs,
          this.floorPlanGraph,
          this.profile,
          requireNonNull(this.fixtureFactory));
    }

    private static List<? extends ResolverEntry> createResolversForFloorPlan(FloorPlanGraph floorPlanGraph) {
      return floorPlanGraph.allWires();
    }

    @SuppressWarnings("unchecked")
    private static List<ResolverEntry> createResolversForComponentSpec(ComponentSpec<?> spec) {
      return new LinkedList<ResolverEntry>() {{
        spec.attributes().stream(
        ).map(
            attribute -> new ResolverEntry(
                printableBiPredicate(
                    () -> String.format("attributeOfRefSpecIsAssignableTo[%s]", shortenedClassName(attribute.spec().attributeType())),
                    (Ref ref, Attribute a) ->
                        attribute.spec().attributeType().isAssignableFrom(ref.spec().attributeType()))
                    .and(
                        printableBiPredicate(
                            () -> String.format("equalTo[%s(%s)]", attribute, attribute.spec()),
                            (ref, a) -> Objects.equals(attribute, a))),
                (Resolver<Attribute, ?>) attribute.defaultValueResolver()
            )
        ).forEach(
            this::add
        );
      }};
    }
  }
}
