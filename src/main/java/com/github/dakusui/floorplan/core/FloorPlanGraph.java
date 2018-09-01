package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.resolver.Resolver;
import com.github.dakusui.floorplan.resolver.ResolverEntry;
import com.github.dakusui.floorplan.resolver.Resolvers;
import com.github.dakusui.floorplan.utils.InternalUtils;

import java.util.*;
import java.util.function.Predicate;

import static com.github.dakusui.floorplan.component.Ref.ref;
import static com.github.dakusui.floorplan.core.FloorPlanGraph.Connector.connector;
import static com.github.dakusui.floorplan.utils.Checks.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface FloorPlanGraph {
  <A extends Attribute> FloorPlanGraph add(ComponentSpec<A> spec, String name);

  FloorPlanGraph add(Ref... refs);

  FloorPlanGraph wire(Ref from,
      Attribute as,
      Ref... tos
  );

  /**
   * By giving {@code null}, you can reset requirements already given to this profile.
   *
   * @param requirement A requirement that describes profile on which this floor can be deployed
   * @return This object.
   */
  FloorPlanGraph requireProfile(Predicate<Profile> requirement);

  Set<Ref> allReferences();

  FloorPlanConfigurator configurator(Policy policy, FloorPlan.Factory floorPlanFactory);

  List<? extends ResolverEntry> allWires();

  boolean isCompatibleWith(Profile profile);

  static FloorPlanGraph create() {
    return new Impl();
  }

  final class Impl implements FloorPlanGraph {
    private final Set<Ref>              refs  = new LinkedHashSet<>();
    private final Map<Connector, Ref[]> wires = new LinkedHashMap<>();
    private       Predicate<Profile>    requirements;

    public Impl() {
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
    public <A extends Attribute> FloorPlanGraph add(ComponentSpec<A> spec, String name) {
      return this.add(ref(spec, name));
    }

    @SuppressWarnings("unchecked")
    public FloorPlanGraph add(Ref... refs) {
      this.refs.addAll(asList(refs));
      return this;
    }

    @SuppressWarnings("unchecked")
    public FloorPlanGraph wire(
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

    @Override
    public FloorPlanGraph requireProfile(Predicate<Profile> requirement) {
      this.requirements = this.requirements == null ?
          requirement :
          this.requirements.and(requirement);
      return this;
    }

    public Set<Ref> allReferences() {
      return Collections.unmodifiableSet(this.refs);
    }

    public FloorPlanConfigurator configurator(Policy policy, FloorPlan.Factory floorPlanFactory) {
      return new FloorPlanConfigurator.Impl(policy, refs, floorPlanFactory);
    }

    public List<? extends ResolverEntry> allWires() {
      return this.wires.entrySet().stream().map(
          entry -> new ResolverEntry(
              InternalUtils.printableBiPredicate(
                  () -> String.format("equalTo(%s)", (Object) entry.getValue()),
                  (ref, attribute) ->
                      entry.getKey().equals(Connector.connector(ref, attribute))
              ),
              Resolver.of(
                  (c -> p -> entry.getKey().fromAttr.valueType().equals(List.class) ?
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
                      ).apply(c, p) :
                      entry.getValue()[0]),
                  () -> String.format("%s->%s", entry.getKey(), Arrays.toString(entry.getValue()))
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
    public boolean isCompatibleWith(Profile profile) {
      return this.requirements == null || this.requirements.test(profile);
    }
  }

  class Connector {
    Attribute fromAttr;
    private Ref       from;

    private Connector(Ref from, Attribute fromAttr) {
      requireArgument(fromAttr, attr -> from.spec() == attr.spec() || attr.spec().attributeType().isAssignableFrom(from.spec().attributeType()));
      this.fromAttr = requireNonNull(fromAttr);
      this.from = requireNonNull(from);
    }

    @Override
    public int hashCode() {
      return this.from.hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
      if (anotherObject instanceof Connector) {
        Connector another = Connector.class.cast(anotherObject);
        return another.from.equals(this.from) && another.fromAttr.equals(this.fromAttr);
      }
      return false;
    }

    @Override
    public String toString() {
      return String.format("%s.%s", from, fromAttr);
    }

    static Connector connector(Ref from, Attribute fromAttr) {
      return new Connector(from, fromAttr);
    }
  }
}