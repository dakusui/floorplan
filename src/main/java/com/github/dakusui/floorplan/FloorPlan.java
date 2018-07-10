package com.github.dakusui.floorplan;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;
import com.github.dakusui.floorplan.resolver.Resolvers;

import java.util.*;

import static com.github.dakusui.floorplan.Connector.connector;
import static com.github.dakusui.floorplan.component.Ref.ref;
import static com.github.dakusui.floorplan.utils.Checks.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public interface FloorPlan<F extends Fixture> {
  <A extends Attribute> FloorPlan add(ComponentSpec<A> spec, String name);

  FloorPlan<F> add(Ref... refs);

  FloorPlan<F> wire(Ref from,
      Attribute as,
      Ref... tos
  );

  Set<Ref> allReferences();

  FixtureConfigurator<F> configurator(Policy policy, Fixture.Factory<F> fixtureFactory);

  List<? extends ResolverEntry> allWires();

  boolean canBeDeployedOn(Profile profile);

  abstract class Base<F extends Fixture> implements FloorPlan<F> {
    private final Set<Ref>              refs  = new LinkedHashSet<>();
    private final Map<Connector, Ref[]> wires = new LinkedHashMap<>();

    public Base() {
    }

    /**
     * If you want to install 2 gateway clusters, you will call this method twice with an instance of
     * a gateway's spec.
     *
     * @param spec A spec instance
     * @param name A name for a configurator which is created from {@code spec}.
     * @return this object.
     */
    @SuppressWarnings("unchecked")
    public <A extends Attribute> FloorPlan<F> add(ComponentSpec<A> spec, String name) {
      return this.add(ref(spec, name));
    }

    @SuppressWarnings("unchecked")
    public FloorPlan<F> add(Ref... refs) {
      this.refs.addAll(asList(refs));
      return this;
    }

    public FloorPlan<F> wire(
        Ref from,
        Attribute as,
        Ref... tos
    ) {
      requireState(
          from, r -> this.refs.contains(from),
          a -> String.format("'%s' is not yet added to this object. (from)", a)
      );
      requireArgument(
          requireNonNull(as),
          a -> Ref.class.equals(a.valueType()) || List.class.equals(a.valueType()),
          a -> String.format("Type of '%s' must either be '%s' or '%s'", a.name(), Ref.class, List.class)
      );
      if (as.valueType().equals(Ref.class))
        if (tos.length != 1)
          throw new IllegalArgumentException(
              String.format(
                  "The number of arguments for 'tos' must exactly be 1 ('%s' is not a list attribute)",
                  as
              ));
      Arrays.stream(tos).forEach(
          each -> requireArgument(
              each,
              this.refs::contains,
              (Ref v) -> String.format("'%s' is not yet added to this object. (to)", v)
          ));
      this.wires.put(
          connector(from, as),
          tos
      );
      return this;
    }

    public Set<Ref> allReferences() {
      return Collections.unmodifiableSet(this.refs);
    }

    public FixtureConfigurator<F> configurator(Policy policy, Fixture.Factory<F> fixtureFactory) {
      return new FixtureConfigurator.Impl<>(policy, refs, fixtureFactory);
    }

    public List<? extends ResolverEntry> allWires() {
      return this.wires.entrySet().stream().map(
          entry -> new ResolverEntry(
              (ref, attribute) ->
                  entry.getKey().equals(Connector.connector(ref, attribute)),
              Resolver.of(
                  a -> c -> p ->
                      a.valueType().equals(List.class) ?
                          Resolvers.listOf(
                              Ref.class,
                              Arrays.stream(
                                  entry.getValue()
                              ).map(
                                  Ref.class::cast
                              ).map(
                                  Resolvers::referenceTo
                              ).collect(
                                  toList()
                              )
                          ).apply(a, c, p) :
                          entry.getValue()[0]
              )
          )
      ).collect(
          toList()
      );
    }

    /**
     * This method checks if a given {@code Profile} can be used with this {@code FloorPlan}
     * object.
     * <p>
     * A user should override this method if there are requirements on floor plans
     * to be used for this profile.
     *
     * @param profile A profile to be checked.
     * @return true - {@code profile} can be used with this floorplan / false - otherwise
     */
    public boolean canBeDeployedOn(Profile profile) {
      return true;
    }
  }
}