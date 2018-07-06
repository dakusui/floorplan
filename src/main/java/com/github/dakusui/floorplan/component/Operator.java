package com.github.dakusui.floorplan.component;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.floorplan.exception.Exceptions;

import java.util.function.Function;

public interface Operator<A extends Attribute> extends Function<Component<A>, Function<Context, Action>> {
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
    static <A extends Attribute> Factory<A> of(Type type, Function<Component<A>, Function<Context, Action>> func) {
      return new Factory<A>() {


        @Override
        public Operator<A> apply(ComponentSpec<A> aComponentSpec) {
          return new Operator<A>() {
            @Override
            public Type type() {
              return type;
            }

            @Override
            public Function<Context, Action> apply(Component<A> component) {
              return context -> context.named(
                  String.format("%s %s", type, component),
                  func.apply(component).apply(context)
              );
            }

            @Override
            public String toString() {
              return "";//String.format("%s %s", type, spec);
            }
          };
        }

        @Override
        public Type type() {
          return type;
        }
      };
    }

    static <A extends Attribute> Factory<A> nop(Type type) {
      return of(
          type,
          component -> Context::nop
      );
    }

    static <A extends Attribute> Factory<A> unsupported(Type type) {
      return of(
          type,
          component -> context -> {
            throw Exceptions.throwUnsupportedOperation(String.format("This operation is not supported by '%s'", component));
          }
      );
    }

    Type type();
  }
}
