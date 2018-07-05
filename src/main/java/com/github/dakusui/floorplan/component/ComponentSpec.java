package com.github.dakusui.floorplan.component;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public interface ComponentSpec<A extends Attribute> {
  Configurator<A> configurator(String id);

  Class<A> attributeType();

  default A[] attributes() {
    return attributeType().getEnumConstants();
  }

  default Attribute.Bean.Builder<A> property(Class<?> type) {
    return new Attribute.Bean.Builder<>(this, type);
  }

  class Impl<A extends Attribute> implements ComponentSpec<A> {
    private final Class<A>                    attributeType;
    private final String                      specName;
    private final Map<Operation, Operator<A>> operators;

    Impl(String specName, Class<A> attributeType, Map<Operation, Operator<A>> operators) {
      this.specName = requireNonNull(specName);
      this.attributeType = requireNonNull(attributeType);
      this.operators = operators;
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
    public String toString() {
      return this.specName;
    }

  }

  class Builder<A extends Attribute> {
    private final Class<A>                    attributeType;
    private final String                      specName;
    private       Map<Operation, Operator<A>> operators;

    public Builder(String specName, Class<A> attributeType) {
      this.specName = requireNonNull(specName);
      this.attributeType = requireNonNull(attributeType);
      this.operators= new HashMap<>();
    }

    public Builder<A> setOperator(Operation op, Operator<A> operator) {
      this.operators.put(requireNonNull(op), requireNonNull(operator));
      return this;
    }

    public ComponentSpec<A> build() {
      return new Impl<A>(this.specName, this.attributeType, this.operators);
    }
  }
}
