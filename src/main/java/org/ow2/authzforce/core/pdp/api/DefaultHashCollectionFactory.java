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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Default implementation of {@link HashCollectionFactory} based on Guava and native Java API. Default expected size is same as for Java API native {@link HashMap}/{@link HashSet}. This implementation
 * does not distinguish mutable from immutable Map, contrary to Koloboke for instance, therefore the returned Map implementation for {@link #newMutableMap()} and {@link #newUpdatableMap()} method
 * variants is the same.
 *
 */
public final class DefaultHashCollectionFactory implements HashCollectionFactory
{

	@Override
	public <K, V> Map<K, V> newMutableMap()
	{
		return new HashMap<>();
	}

	@Override
	public <K, V> Map<K, V> newUpdatableMap()
	{
		return new HashMap<>();
	}

	@Override
	public <K, V> Map<K, V> newUpdatableMap(final int expectedSize)
	{
		return Maps.newHashMapWithExpectedSize(expectedSize);
	}

	@Override
	public <K, V> Map<K, V> newUpdatableMap(final Map<? extends K, ? extends V> map)
	{
		return new HashMap<>(map);
	}

	@Override
	public <K, V> Map<K, V> newUpdatableMap(final Map<? extends K, ? extends V> map1, final Map<? extends K, ? extends V> map2)
	{
		final Map<K, V> merge = new HashMap<>(map1);
		merge.putAll(map2);
		return merge;
	}

	@Override
	public <K, V> Map<K, V> newUpdatableMap(final Map<? extends K, ? extends V> map1, final Map<? extends K, ? extends V> map2, final Map<? extends K, ? extends V> map3)
	{
		final Map<K, V> merge = new HashMap<>(map1);
		merge.putAll(map2);
		merge.putAll(map3);
		return merge;
	}

	@Override
	public <K, V> Map<K, V> newImmutableMap(final Map<? extends K, ? extends V> map)
	{
		return ImmutableMap.copyOf(map);
	}

	@Override
	public <K, V> Map<K, V> newImmutableMap(final Map<? extends K, ? extends V> map1, final Map<? extends K, ? extends V> map2)
	{
		return ImmutableMap.copyOf(newUpdatableMap(map1, map2));
	}

	@Override
	public <K, V> Map<K, V> newImmutableMap(final K k1, final V v1)
	{
		return ImmutableMap.of(k1, v1);
	}

	@Override
	public <K, V> Map<K, V> newImmutableMap(final K k1, final V v1, final K k2, final V v2)
	{
		return ImmutableMap.of(k1, v1, k2, v2);
	}

	@Override
	public <K, V> Map<K, V> newImmutableMap(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3)
	{
		return ImmutableMap.of(k1, v1, k2, v2, k3, v3);
	}

	@Override
	public <E> Set<E> newUpdatableSet()
	{
		return new HashSet<>();
	}

	@Override
	public <E> Set<E> newUpdatableSet(final int expectedSize)
	{
		return Sets.newHashSetWithExpectedSize(expectedSize);
	}

	@Override
	public <E> Set<E> newUpdatableSet(final Iterable<? extends E> elements)
	{
		return Sets.newHashSet(elements);
	}

	@Override
	public <E> Set<E> newImmutableSet(final E[] elements)
	{
		return ImmutableSet.copyOf(elements);
	}

	@Override
	public <E> Set<E> newImmutableSet(final Iterable<? extends E> elements)
	{
		return ImmutableSet.copyOf(elements);
	}

	@Override
	public <E> Set<E> newImmutableSet(final Set<? extends E> set1, final Set<? extends E> set2)
	{
		return Sets.union(set1, set2);
	}

	@Override
	public <E> Set<E> newImmutableSet(final E e1)
	{
		return ImmutableSet.of(e1);
	}

}
