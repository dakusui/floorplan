package com.github.dakusui.floorplan.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.floorplan.utils.InternalUtils.*;
import static java.util.Objects.requireNonNull;

public interface ComponentSpec<A extends Attribute> {
  Configurator<A> configurator(String id);

  Class<A> attributeType();

  default List<A> attributes() {
    return Attribute.attributes(attributeType());
  }

  default Attribute.Bean.Builder<A> property(Class<?> type) {
    return new Attribute.Bean.Builder<>(this, type, isInstanceOf(type));
  }

  /**
   * Returns a builder to create an attribute that references to another component
   * of a specified type.
   *
   * @param spec A type of a component referenced by an attribute built by returned
   *             builder.
   * @return An attribute bean builder.
   */
  default Attribute.Bean.Builder<A> property(ComponentSpec<?> spec) {
    return new Attribute.Bean.Builder<>(
        this,
        Ref.class,
        isInstanceOf(Ref.class).and(hasSpecOf(spec)));
  }

  default Attribute.Bean.Builder<A> listPropertyOf(Class<?> type) {
    return new Attribute.Bean.Builder<>(
        this,
        List.class,
        isInstanceOf(List.class).and(forAll(isInstanceOf(type)))
    );
  }

  default Attribute.Bean.Builder<A> listPropertyOf(ComponentSpec<?> spec) {
    return new Attribute.Bean.Builder<>(
        this,
        List.class,
        isInstanceOf(List.class).and(forAll(isInstanceOf(Ref.class).and(hasSpecOf(spec))))
    );
  }

  Map<Operator.Type, Operator.Factory<A>> operatorFactories();

  class Impl<A extends Attribute> implements ComponentSpec<A> {
    private final Class<A>                                attributeType;
    private final String                                  specName;
    private final Map<Operator.Type, Operator.Factory<A>> operatorFactories;

    Impl(String specName, Class<A> attributeType, Map<Operator.Type, Operator.Factory<A>> operatorFactories) {
      this.specName = requireNonNull(specName);
      this.attributeType = requireNonNull(attributeType);
      this.operatorFactories = operatorFactories;
    }

    @Override
    public Configurator<A> configurator(String id) {
      return new Configurator.Impl<>(this, id);
    }

    @Override
    public Class<A> attributeType() {
      return this.attributeType;
    }

    @Override
    public Map<Operator.Type, Operator.Factory<A>> operatorFactories() {
      return this.operatorFactories;
    }

    @Override
    public String toString() {
      return this.specName;
    }

  }

  class Builder<A extends Attribute> {
    private final Class<A>                             attributeType;
    private final String                               specName;
    private       Map<Operator.Type, Operator.Factory<A>> operatorFactories;

    public Builder(String specName, Class<A> attributeType) {
      this.specName = requireNonNull(specName);
      this.attributeType = requireNonNull(attributeType);
      this.operatorFactories = new HashMap<>();
    }

    public Builder(Class<A> attributeType) {
      this(requireNonNull(requireNonNull(attributeType).getEnclosingClass()).getSimpleName(), attributeType);
    }

    public Builder<A> addOperatorFactory(Operator.Factory<A> operatorFactory) {
      this.operatorFactories.put(requireNonNull(operatorFactory.type()), requireNonNull(operatorFactory));
      return this;
    }

    public ComponentSpec<A> build() {
      return new Impl<>(this.specName, this.attributeType, this.operatorFactories);
    }
  }
}
