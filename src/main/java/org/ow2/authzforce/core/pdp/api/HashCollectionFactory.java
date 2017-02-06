/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.Map;
import java.util.Set;

/**
 * Factory method to construct HashMaps/HashSets. Implement this interface to allow AuthZForce code to use alternatives to Java native implementations, which alternatives must support:
 * <ol>
 * <li>Immutable HashMap/HashSet implementations.</li>
 * <li>Mutable HashMap/HashSet implementations accepting an expected size as constructor parameter.</li>
 * </ol>
 * Ideally, the alternative should support <a href="http://leventov.github.io/Koloboke/api/1.0/java8/overview-summary.html#mutability">updatable</a> HashMaps/HashSets accepting an expected size as
 * constructor parameter as well.
 * <p>
 * 
 * @see "http://java-performance.info/hashmap-overview-jdk-fastutil-goldman-sachs-hppc-koloboke-trove-january-2015/"
 *
 */
public interface HashCollectionFactory
{

	/**
	 * Constructs a new empty mutable map of the default expected size (depending on the implementation).
	 * 
	 * @return a new empty mutable map
	 */
	<K, V> Map<K, V> newMutableMap();

	/**
	 * Constructs a new empty updatable map of the default expected size (depending on the underlying implementation).
	 * 
	 * @return new empty updatable map
	 */
	<K, V> Map<K, V> newUpdatableMap();

	/**
	 * Constructs a new empty updatable map of the given expected size.
	 * 
	 * @param expectedSize
	 *            expected size of the returned map
	 * @return a new empty updatable map of the given expected size (postive)
	 * @throws IllegalArgumentException
	 *             if {@code expectedSize} is negative
	 */
	<K, V> Map<K, V> newUpdatableMap(final int expectedSize) throws IllegalArgumentException;

	/**
	 * Constructs a new updatable map with the same mappings as the specified map.
	 * 
	 * @param map
	 *            the map whose mappings are to be placed in the returned map
	 * @return a new updatable map with the same mappings as the specified map
	 */
	<K, V> Map<K, V> newUpdatableMap(final Map<? extends K, ? extends V> map);

	/**
	 * Constructs a new updatable map which merge the mappings of the specified maps. On conflict, mappings from the map2 have priority over mappings from the map1 with the same keys.
	 * 
	 * @param map1
	 *            the first map to merge
	 * @param map2
	 *            the second map to merge
	 * @return a new updatable map which merges the mappings of the specified maps
	 */
	<K, V> Map<K, V> newUpdatableMap(final Map<? extends K, ? extends V> map1, final Map<? extends K, ? extends V> map2);

	/**
	 * Constructs a new updatable map which merge the mappings of the specified maps. On conflict, mappings from the maps passed later in the argument list have priority over mappings from the maps
	 * passed earlier with the same keys.
	 * 
	 * @param map1
	 *            the first map to merge
	 * @param map2
	 *            the second map to merge
	 * @param map3
	 *            the third map to merge
	 * @return a new updatable map which merges the mappings of the specified maps
	 */
	<K, V> Map<K, V> newUpdatableMap(final Map<? extends K, ? extends V> map1, final Map<? extends K, ? extends V> map2, final Map<? extends K, ? extends V> map3);

	/**
	 * Constructs a new immutable map with the same mappings as the specified map.
	 * 
	 * @param map
	 *            the map whose mappings are to be placed in the returned map
	 * @return a new immutable map with the same mappings as the specified map
	 */
	<K, V> Map<K, V> newImmutableMap(final Map<? extends K, ? extends V> map);

	/**
	 * Constructs a new immutable map which merges the mappings of the specified maps. On conflict, mappings from the map2 have priority over mappings from the map1 with the same keys.
	 * 
	 * @param map1
	 *            the first map to merge
	 * @param map2
	 *            the second map to merge
	 * 
	 * @return a new immutable map which merges the mappings of the specified maps
	 */
	<K, V> Map<K, V> newImmutableMap(final Map<? extends K, ? extends V> map1, final Map<? extends K, ? extends V> map2);

	/**
	 * Constructs a new immutable map of the single specified mapping.
	 * 
	 * @param k1
	 *            the key of the sole mapping
	 * @param v1
	 *            the value of the sole mapping
	 * @return a new immutable map of the single specified mapping
	 */
	<K, V> Map<K, V> newImmutableMap(final K k1, final V v1);

	/**
	 * Constructs a new immutable map of the two specified mappings.
	 * 
	 * @param k1
	 *            the key of the first mapping
	 * @param v1
	 *            the value of the first mapping
	 * @param k2
	 *            the key of the second mapping
	 * @param v2
	 *            the value of the second mapping
	 * @return Returns an immutable map containing the given entries, in order.
	 */
	<K, V> Map<K, V> newImmutableMap(final K k1, final V v1, final K k2, final V v2);

	/**
	 * Constructs a new immutable map of the three specified mappings.
	 * 
	 * @param k1
	 *            the key of the first mapping
	 * @param v1
	 *            the value of the first mapping
	 * @param k2
	 *            the key of the second mapping
	 * @param v2
	 *            the value of the second mapping
	 * @param k3
	 *            the key of the third mapping
	 * @param v3
	 *            the value of the third mapping
	 * @return Returns an immutable map containing the given entries, in order.
	 */
	<K, V> Map<K, V> newImmutableMap(final K k1, final V v1, final K k2, final V v2, K k3, V v3);

	/**
	 * Constructs a new empty updatable set of the default expected size (depending on the underlying implementation).
	 * 
	 * @return a new empty updatable set
	 */
	<E> Set<E> newUpdatableSet();

	/**
	 * Constructs a new empty updatable set of the given expected size.
	 * 
	 * @param expectedSize
	 *            the expected size of the returned set
	 * @return a new empty updatable set of the given expected size (positive)
	 * @throws IllegalArgumentException
	 *             if {@code expectedSize} is negative
	 */
	<E> Set<E> newUpdatableSet(final int expectedSize) throws IllegalArgumentException;

	/**
	 * Constructs a new updatable set containing the elements in the specified iterable.
	 * 
	 * @param elements
	 *            the iterable whose elements are to be placed into the returned set
	 * @return a new updatable set of the elements of the specified iterable
	 */
	<E> Set<E> newUpdatableSet(final Iterable<? extends E> elements);

	/**
	 * Constructs a new immutable set of elements from the given array.
	 * 
	 * @param elements
	 *            the array whose elements are to be placed into the returned set
	 * @return a new immutable set of elements from the given array
	 */
	<E> Set<E> newImmutableSet(final E[] elements);

	/**
	 * Constructs a new immutable set containing the elements in the specified iterable.
	 * 
	 * @param elements
	 *            the iterable whose elements are to be placed into the returned set
	 * @return a new immutable set of the elements of the specified iterable
	 */
	<E> Set<E> newImmutableSet(final Iterable<? extends E> elements);

	/**
	 * Constructs a new immutable set which merges the elements of the specified sets.
	 * 
	 * @param set1
	 *            the first source of elements for the returned set
	 * @param set2
	 *            the second source of elements for the returned set
	 * 
	 * @return a new immutable set which merges the elements of the specified sets
	 */
	<E> Set<E> newImmutableSet(final Set<? extends E> set1, final Set<? extends E> set2);

	/**
	 * Constructs a new immutable singleton set of the given element.
	 * 
	 * @param e1
	 *            the sole element
	 * @return a new immutable singleton set of the given element
	 */
	<E> Set<E> newImmutableSet(final E e1);

}
