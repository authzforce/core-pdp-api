/**
 * Copyright 2012-2019 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
