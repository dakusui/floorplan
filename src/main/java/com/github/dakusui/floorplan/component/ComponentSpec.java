package com.github.dakusui.floorplan.component;

import com.github.dakusui.floorplan.utils.InternalUtils;

import java.util.List;
import java.util.Optional;

import static com.github.dakusui.floorplan.utils.InternalUtils.*;
import static java.util.Objects.requireNonNull;

/**
 * An interface to describe a certain component's specification.
 *
 * @param <A> A type of attributes to describe the component.
 * @see Attribute.Definition
 * @see Attribute
 */
public interface ComponentSpec<A extends Attribute> {
  <C extends Component<A>> Class<C> componentType();

  /**
   * Creates a new {@code Configurator} instance with specified {@code id} of a
   * component described by this object.
   *
   * @param id An id of a new configurator.
   * @return A new configurator.
   */
  Configurator<A> configurator(String id);

  /**
   * A class of attributes that describe a components specification
   *
   * @return A class of an attribute.
   */
  Class<A> attributeType();

  Optional<ComponentSpec> parentSpec();

  /**
   * Returns a list of all attributes to describe the component.
   *
   * @return A list of attributes.
   */
  default List<Attribute> attributes() {
    return InternalUtils.attributes(attributeType());
  }


  /**
   * Returns a new builder of an attribute definition for a given type.
   *
   * @param type A type of an attribute value for which the definition works.
   * @return A new attribute definition builder.
   */
  default Attribute.Definition.Builder<A> property(Class<?> type) {
    return new Attribute.Definition.Builder<>(this, type, isInstanceOf(type));
  }

  /**
   * Returns a new builder to create an attribute that references to another component
   * of a specified type.
   *
   * @param spec A type of a component referenced by an attribute built by returned
   *             builder.
   * @return An new attribute definition builder.
   */
  default Attribute.Definition.Builder<A> property(ComponentSpec<?> spec) {
    return new Attribute.Definition.Builder<>(
        this,
        Ref.class,
        isInstanceOf(Ref.class).and(hasCompatibleSpecWith(spec)));
  }

  /**
   * Returns a new builder of an attribute definition for a list of given type.
   *
   * @param type A type of elements in a new attribute value.
   * @return A new attribute definition builder.
   */
  default Attribute.Definition.Builder<A> listPropertyOf(Class<?> type) {
    return new Attribute.Definition.Builder<>(
        this,
        List.class,
        isInstanceOf(List.class).and(forAll(isInstanceOf(type)))
    );
  }

  /**
   * Returns a new builder of an attribute definition for a list of references to
   * component instances/configurators of a given {@code spec}.
   *
   * @param spec A spec of elements in a new attribute value reference to.
   * @return A new attribute definition builder.
   */
  default Attribute.Definition.Builder<A> listPropertyOf(ComponentSpec<?> spec) {
    return new Attribute.Definition.Builder<>(
        this,
        List.class,
        isInstanceOf(List.class).and(forAll(isInstanceOf(Ref.class).and(hasSpecOf(spec))))
    );
  }

  static <A extends Attribute> ComponentSpec<A> create(
      Class<? extends Component<A>> componentType,
      Class<A> attributeType) {
    return new ComponentSpec.Impl<>(componentType.getSimpleName(), attributeType, componentType, null);
  }

  static <A extends Attribute> ComponentSpec<A> create(
      Class<? extends Component<A>> componentType
  ) {
    return create(componentType, figureOutAttributeTypeFor(componentType));
  }

  class Impl<A extends Attribute> implements ComponentSpec<A> {
    private final Class<A>                      attributeType;
    private final String                        specName;
    private final Class<? extends Component<A>> componentType;
    private final ComponentSpec                 parentSpec;

    Impl(String specName, Class<A> attributeType, Class<? extends Component<A>> componentType, ComponentSpec parentSpec) {
      this.specName = requireNonNull(specName);
      this.attributeType = requireNonNull(attributeType);
      this.componentType = componentType;
      this.parentSpec = parentSpec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Component<A>> Class<C> componentType() {
      return (Class<C>) componentType;
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
    public Optional<ComponentSpec> parentSpec() {
      return Optional.ofNullable(this.parentSpec);
    }

    @Override
    public String toString() {
      return this.specName;
    }

  }

  /**
   * A builder for a {@code ComponentSpec}.
   *
   * @param <A> A type of attribute that characterizes an instance of the component spec.
   */
  class Builder<A extends Attribute> {
    private final Class<A>                   attributeType;
    private final String                     specName;
    private       Class<? extends Component> componentType = Component.class;
    private       ComponentSpec              parent;

    /**
     * Creates a new instance of this class with given {@code specName} and {@code attributeType}.
     *
     * @param specName      A name of the spec.
     * @param attributeType A type of attributes that characterize an instance of
     *                      a given spec.
     */
    public Builder(String specName, Class<A> attributeType) {
      this.specName = requireNonNull(specName);
      this.attributeType = requireNonNull(attributeType);
    }

    public Builder(Class<A> attributeType) {
      this(attributeType.getSimpleName(), attributeType);
    }

    public Builder<A> componentType(Class<? extends Component> klass) {
      this.componentType = requireNonNull(klass);
      return this;
    }

    public <B extends Attribute> Builder<A> inherit(ComponentSpec<B> parent) {
      this.parent = requireNonNull(parent);
      return this;
    }

    @SuppressWarnings("unchecked")
    public ComponentSpec<A> build() {
      return new Impl<>(this.specName, this.attributeType, (Class<? extends Component<A>>) this.componentType, this.parent);
    }
  }
}
