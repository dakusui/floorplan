package com.github.dakusui.floorplan.resolver;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.utils.FloorPlanUtils;

import java.util.List;

import static com.github.dakusui.floorplan.exception.Exceptions.typeMismatch;
import static com.github.dakusui.floorplan.utils.Checks.require;
import static com.github.dakusui.floorplan.utils.Checks.requireNonNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum Resolvers {
	;

	public static <A extends Attribute, T> Resolver<A, T> immediate(T value) {
		return Resolver.of(
				a -> c -> p -> value,
				() -> String.format("immediate(%s)", value)
		);
	}

	public static <A extends Attribute> Resolver<A, String> instanceId() {
		return Resolver.of(
				a -> c -> p -> c.ref().id(),
				() -> "instanceId()"
		);
	}

	public static <A extends Attribute> Resolver<A, Ref> referenceTo(Ref ref) {
		return Resolver.of(
				a -> c -> p -> ref,
				() -> String.format("referenceTo(component:%s)", ref)
		);
	}

	public static <A extends Attribute, T> Resolver<A, T> referenceTo(A another) {
		requireNonNull(another);
		return Resolver.of(
				a -> c -> p -> FloorPlanUtils.resolve(another, c, p),
				() -> String.format("referenceTo(attr:%s)", another)
		);
	}

	@SuppressWarnings("unchecked")
	public static <A extends Attribute, B extends Attribute, R> Resolver<A, R> attributeValueOf(B attr, Resolver<A, Ref> holder) {
		return Resolver.of(
				a -> c -> p -> FloorPlanUtils.resolve(attr, p.fixtureConfigurator().lookUp(holder.apply(a).apply(c).apply(p)), p),
				() -> String.format("attributeValueOf(%s, %s)", attr, holder)
		);
	}

	public static <A extends Attribute, T> Resolver<A, T> profileValue(String key) {
		return Resolver.of(
				a -> c -> p -> p.profile().<A, T>resolverFor(key).apply(a).apply(c).apply(p),
				() -> String.format("profileValueOf(%s)", key)
		);
	}

	/**
	 * Returned resolver will give a value specified by a {@code key} from a slot.
	 * This method is only useful when {@code valueType} of an attribute the returned resolver belongs to is the same
	 * as the one returned by the resolver.
	 *
	 * @param key A key to specify a value in a slot
	 * @param <A> Type of attribute
	 * @param <T> Type of returned value.
	 * @return A created resolver.
	 */
	@SuppressWarnings("unchecked")
	public static <A extends Attribute, T> Resolver<A, T> slotValue(String key) {
		return Resolver.of(
				a -> c -> p -> p.profile().slotFor(c.ref()).<A, T>resolverFor((Class<T>) a.valueType(), key).apply(a).apply(c).apply(p),
				() -> String.format("slotValueOf(%s)", key)
		);
	}

	public static <A extends Attribute, T> Resolver<A, T> slotValue(Class<T> requestedType, String key) {
		return Resolver.of(
				a -> c -> p -> p.profile().slotFor(c.ref()).<A, T>resolverFor(requestedType, key).apply(a).apply(c).apply(p),
				() -> String.format("slotValueOf(%s)", key)
		);
	}

	@SuppressWarnings("unchecked")
	public static <A extends Attribute, E>
	Resolver<A, List<E>> listOf(Class<E> type, Resolver<A, ? extends E>... resolvers) {
		return listOf(type, asList(resolvers));
	}

	@SuppressWarnings("unchecked")
	public static <A extends Attribute, E>
	Resolver<A, List<E>> listOf(Class<E> type, List<Resolver<A, ? extends E>> resolvers) {
		return Resolver.of(
				a -> c -> p ->
						resolvers.stream().map(
								resolver -> resolver.apply(a, c, p)
						).map(
								e -> require(e, o -> o == null || type.isAssignableFrom(o.getClass()), typeMismatch(a, e))
						).collect(toList()),
				() -> String.format(
						"listOf(%s, %s)",
						type.getSimpleName(),
						String.join(
								",",
								resolvers.stream().map(Object::toString).collect(toList())))
		);
	}

	public static <A extends Attribute, T, R>
	Resolver<A, R> transform(Resolver<A, T> resolver, Mapper<A, T, R> mapper) {
		return Resolver.of(
				a -> c -> p ->
						mapper.apply(a, c, p, resolver.apply(a, c, p)),
				() -> String.format(
						"transform(%s, %s)",
						resolver,
						mapper
				)
		);
	}

	public static <A extends Attribute, T, R>
	Resolver<A, List<R>> transformList(Resolver<A, List<T>> resolver, Mapper<A, T, R> mapper) {
		return Resolver.of(
				a -> c -> p ->
						resolver.apply(a, c, p).stream().map(t -> mapper.apply(a, c, p, t)).collect(toList()),
				() -> String.format(
						"transformList(%s, %s)",
						resolver,
						mapper
				)
		);
	}

	public static <A extends Attribute, E>
	Resolver<A, Integer> sizeOf(Resolver<A, List<E>> resolver) {
		return a -> c -> p -> resolver.apply(a, c, p).size();
	}


	/**
	 * Returns a resolver that always throw a missing value exception.
	 * This method is useful to define an attribute that does not have a default value,
	 * i.e., a 'required' attribute.
	 *
	 * @param <A> Type of attribute
	 * @param <T> Type of an expected value.
	 * @return A resolver that throws an exception always when applied.
	 */
	public static <A extends Attribute, T> Resolver<A, T> nothing() {
		return Resolver.of(
				a -> c -> p -> {
					throw Exceptions.missingValue(c.ref(), a).get();
				},
				() -> "nothing"
		);
	}

}
