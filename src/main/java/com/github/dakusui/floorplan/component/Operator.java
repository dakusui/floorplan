package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.floorplan.exception.Exceptions;

import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.named;

public interface Operator<A extends Attribute> extends Function<Component<A>, Action> {
  /**
   * This enumerates categories of actions that can be performed on a component.
   */
  enum Type {
    /**
     * An operation that belongs to this category installs a component from which
     * it is created.
     */
    INSTALL,
    /**
     * An operation that belongs to this category starts a component from which
     * it is created.
     */
    START,
    /**
     * An operation that belongs to this category stops a component from which
     * it is created.
     */
    STOP,
    /**
     * An operation that belongs to this category kills a component from which
     * it is created.
     */
    NUKE,
    /**
     * An operation that belongs to this category uninstalls a component from which
     * it is created.
     */
    UNINSTALL
  }

  Type type();

  interface Factory<A extends Attribute> extends Function<ComponentSpec<A>, Operator<A>> {
    static <A extends Attribute> Factory<A> of(Type type, Function<Component<A>, Action> func) {
      return new Factory<A>() {
        @Override
        public Operator<A> apply(ComponentSpec<A> aComponentSpec) {
          return new Operator<A>() {
            @Override
            public Type type() {
              return type;
            }

            @Override
            public Action apply(Component<A> component) {
              return named(
                  String.format("%s %s", type, component),
                  func.apply(component)
              );
            }

            @Override
            public String toString() {
              return String.format("operator(%s %s)", type, aComponentSpec);
            }
          };
        }

        @Override
        public Type type() {
          return type;
        }
      };
    }

    static Factory nop(Type type) {
      return of(
          type,
          component -> ActionSupport.nop()
      );
    }

    static Factory unsupported(Type type) {
      return of(
          type,
          component -> {
            throw Exceptions.throwUnsupportedOperation(String.format("This operation is not supported by '%s'", component));
          }
      );
    }

    Type type();
  }
}
