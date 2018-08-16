package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Profile;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;
import static java.util.Collections.unmodifiableList;

public interface FixtureDescriptor {
  Profile profile();

  List<ComponentSpec> specs();

  List<Ref> refs();

  List<Wire> wires();

  List<AttributeConfigurator> attributes();

  List<Consumer<FixtureConfigurator>> operatorFactoryAdders();

  class Wire {
    public final Ref       from;
    public final Attribute as;
    public final Ref[]     tos;

    Wire(Ref from, Attribute as, Ref[] tos) {
      this.from = from;
      this.as = as;
      this.tos = tos;
    }
  }

  class AttributeConfigurator<A extends Attribute> {
    public final Ref            target;
    public final A              attribute;
    public final Resolver<A, ?> resolver;

    AttributeConfigurator(Ref target, A attribute, Resolver<A, ?> resolver) {
      this.target = target;
      this.attribute = attribute;
      this.resolver = resolver;
    }
  }

  class Builder {
    private       List<ComponentSpec>                 specs                 = new LinkedList<>();
    private       List<Ref>                           refs                  = new LinkedList<>();
    private       List<Wire>                          wires                 = new LinkedList<>();
    private       List<AttributeConfigurator>         configs               = new LinkedList<>();
    private final Profile                             profile;
    private       List<Consumer<FixtureConfigurator>> operatorFactoryAdders = new LinkedList<>();


    public Builder(Profile profile) {
      this.profile = requireNonNull(profile);
    }

    public Builder add(Ref... refs) {
      Arrays.stream(refs).forEach(
          this::addRef
      );
      return this;
    }

    private void addRef(Ref ref) {
      this.addSpec(ref.spec());
      if (!this.refs.contains(ref))
        this.refs.add(ref);
    }

    private void addSpec(ComponentSpec spec) {
      if (!this.specs.contains(spec))
        this.specs.add(spec);
    }

    public Builder wire(
        Ref from,
        Attribute as,
        Ref... tos
    ) {
      this.add(from);
      this.add(tos);
      this.wires.add(new Wire(from, as, tos));
      return this;
    }

    public Builder wire(
        List<Ref> from,
        Attribute as,
        List<Ref> tos
    ) {
      from.forEach(ref -> wire(ref, as, tos.toArray(new Ref[0])));
      return this;
    }

    public <A extends Attribute> Builder configure(
        Ref from,
        A as,
        Resolver<A, ?> value
    ) {
      this.add(from);
      this.configs.add(new AttributeConfigurator<>(from, as, value));
      return this;
    }

    public Builder configureWithValue(Ref from,
        Attribute as,
        Object value
    ) {
      return configure(from, as, immediate(value));
    }

    public FixtureDescriptor build() {
      return new FixtureDescriptor() {
        @Override
        public Profile profile() {
          return profile;
        }

        @Override
        public List<ComponentSpec> specs() {
          return unmodifiableList(specs);
        }

        @Override
        public List<Ref> refs() {
          return unmodifiableList(refs);
        }

        @Override
        public List<Wire> wires() {
          return unmodifiableList(wires);
        }

        @Override
        public List<AttributeConfigurator> attributes() {
          return unmodifiableList(configs);
        }

        @Override
        public List<Consumer<FixtureConfigurator>> operatorFactoryAdders() {
          return unmodifiableList(operatorFactoryAdders);
        }
      };
    }
  }
}
