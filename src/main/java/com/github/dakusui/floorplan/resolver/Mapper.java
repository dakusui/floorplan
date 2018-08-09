package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Mapper<A extends Attribute, T, R> extends Function<Configurator<A>, Function<Policy, Function<T, R>>> {
	default R apply(Configurator<A> configurator, Policy policy, T value) {
		return this.apply(configurator).apply(policy).apply(value);
	}

	static <A extends Attribute, T, R> Mapper<A, T, R> of(Function<Configurator<A>, Function<Policy, Function<T, R>>> func) {
		return of(func, () -> "Mapper(noname)");
	}

	static <A extends Attribute, T, R> Mapper<A, T, R> of(Function<Configurator<A>, Function<Policy, Function<T, R>>> func, Supplier<String> messageSupplier) {
		return new Mapper<A, T, R>() {
			@Override
			public Function<Policy, Function<T, R>> apply(Configurator<A> aConfigurator) {
				return p -> t -> func.apply(aConfigurator).apply(p).apply(t);
			}

			@Override
			public String toString() {
				return messageSupplier.get();
			}
		};
	}

	static <A extends Attribute, T, R> Mapper<A, T, R> create(Function<T, R> func) {
		return of(c -> p -> func);
	}
}
