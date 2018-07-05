package com.github.dakusui.floorplan.policy;

import com.github.dakusui.floorplan.DeploymentConfigurator;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;

import java.util.*;

import static com.github.dakusui.floorplan.exception.Exceptions.noSuchElement;
import static com.github.dakusui.floorplan.utils.Checks.requireArgument;
import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Policy {
  <A extends Attribute, T> Resolver<A, T> fallbackResolverFor(Ref ref, A attr);

  Profile profile();

  /**
   * Returns a {@code DeploymentConfigurator} instance.
   *
   * @return A {@code DeploymentConfigurator}.
   */
  DeploymentConfigurator deploymentConfigurator();

  class Impl implements Policy {
    private final DeploymentConfigurator deploymentConfigurator;
    private final Profile                profile;

    Impl(List<ResolverEntry> resolvers, Collection<ComponentSpec<?>> specs, FloorPlan floorPlan, Profile profile) {
      requireArgument(
          floorPlan,
          f -> f.allReferences().stream().allMatch(ref -> specs.contains(ref.spec())),
          () -> String.format(
              "References using unknown specs are found.: %s",
              floorPlan.allReferences().stream().filter(ref -> !specs.contains(ref.spec())).collect(toList())
          )
      );
      this.resolvers = unmodifiableList(requireNonNull(resolvers));
      this.deploymentConfigurator = requireNonNull(floorPlan).configurator(this);
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
    public DeploymentConfigurator deploymentConfigurator() {
      return this.deploymentConfigurator;
    }

  }

  class Builder {
    private final List<ResolverEntry>    resolvers = new LinkedList<>();
    private final List<ComponentSpec<?>> specs     = new LinkedList<>();
    private       FloorPlan              floorPlan = null;
    private       Profile                profile;

    public Builder() {
    }

    public Builder setFloorPlan(FloorPlan floorPlan) {
      requireState(this, v -> v.floorPlan == null);
      this.resolvers.addAll(createResolversForFloorPlan(floorPlan));
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

    public Policy build() {
      return new Impl(new LinkedList<ResolverEntry>(resolvers) {{
        reverse(this);
      }}, this.specs,
          this.floorPlan,
          this.profile
      );
    }

    private static List<? extends ResolverEntry> createResolversForFloorPlan(FloorPlan floorPlan) {
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
