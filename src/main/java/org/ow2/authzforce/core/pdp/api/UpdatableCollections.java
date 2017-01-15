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
package org.ow2.authzforce.core.pdp.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for {@code UpdatableList}s
 * 
 */
public final class UpdatableCollections
{

	private UpdatableCollections()
	{
		// prevent instantiation
	}

	/**
	 * {@link ArrayList}-based implementation of {@link UpdatableList}
	 * 
	 * @param <E>
	 *            type of elements in this list
	 */
	private static final class UpdatableArrayList<E> implements UpdatableList<E>
	{
		private final List<E> list = new ArrayList<>();

		private UpdatableArrayList()
		{
			// list initialized above
		}

		private UpdatableArrayList(final E e)
		{
			list.add(e);
		}

		@Override
		public boolean add(final E e) throws NullPointerException
		{
			return list.add(e);
		}

		@Override
		public boolean addAll(final Collection<? extends E> elements) throws NullPointerException
		{
			return list.addAll(elements);
		}

		@Override
		public ImmutableList<E> copy()
		{
			return ImmutableList.copyOf(list);
		}

	}

	private static final class EmptyList<E> implements UpdatableList<E>
	{

		@Override
		public boolean add(final E e)
		{
			return false;
		}

		@Override
		public boolean addAll(final Collection<? extends E> elements)
		{
			return false;
		}

		@Override
		public ImmutableList<E> copy()
		{
			return ImmutableList.of();
		}

	}

	private static final UpdatableList<?> EMPTY_LIST = new EmptyList<>();

	/**
	 * Get instance of UpdatableList that does not update anything and {@link UpdatableList#copy()} always return an empty list. This implementation does not raise any exception on
	 * {@link UpdatableList#add(Object)} and {@link UpdatableList#addAll(Collection)} method but merely return false always. This is useful merely for polymorphism.
	 * 
	 * @return "empty" list, i.e. list that silently ignores updates and always stays empty
	 */
	@SuppressWarnings("unchecked")
	public static <E> UpdatableList<E> emptyList()
	{
		return (UpdatableList<E>) EMPTY_LIST;
	}

	/**
	 * Create new instance of UpdatableList not accepting null values
	 * 
	 * @return new instance
	 */
	public static <E> UpdatableList<E> newUpdatableList()
	{
		return new UpdatableArrayList<>();
	}

	private static final class EmptySet<E> implements UpdatableSet<E>
	{

		@Override
		public boolean add(final E e)
		{
			return false;
		}

		@Override
		public boolean addAll(final Collection<? extends E> elements)
		{
			return false;
		}

		@Override
		public ImmutableSet<E> copy()
		{
			return ImmutableSet.of();
		}

	}

	private static final UpdatableSet<?> EMPTY_SET = new EmptySet<>();

	/**
	 * Get instance of UpdatableSet that does not update anything and {@link UpdatableSet#copy()} always return an empty Set. This implementation does not raise any exception on
	 * {@link UpdatableSet#add(Object)} and {@link UpdatableSet#addAll(Collection)} method but merely return false always. This is useful merely for polymorphism.
	 * 
	 * @return "empty" Set, i.e. list that silently ignores updates and always stays empty
	 */
	@SuppressWarnings("unchecked")
	public static <E> UpdatableSet<E> emptySet()
	{
		return (UpdatableSet<E>) EMPTY_SET;
	}

	/**
	 * {@link HashObjSet}-based implementation of {@link UpdatableSet}
	 * 
	 * @param <E>
	 *            type of elements in this Set
	 */
	private static final class UpdatableHashSet<E> implements UpdatableSet<E>
	{
		private final Set<E> set = HashCollections.newUpdatableSet();

		private UpdatableHashSet()
		{
			// Set initialized above
		}

		private UpdatableHashSet(final E e)
		{
			set.add(e);
		}

		@Override
		public boolean add(final E e) throws NullPointerException
		{
			return set.add(e);
		}

		@Override
		public boolean addAll(final Collection<? extends E> elements) throws NullPointerException
		{
			return set.addAll(elements);
		}

		@Override
		public ImmutableSet<E> copy()
		{
			return ImmutableSet.copyOf(set);
		}

	}

	/**
	 * Create new instance of UpdatableSet not accepting null values
	 * 
	 * @return new instance
	 */
	public static <E> UpdatableSet<E> newUpdatableSet()
	{
		return new UpdatableHashSet<>();
	}

	private static final class EmptyMap<K, V> implements UpdatableMap<K, V>
	{

		@Override
		public V put(final K key, final V value)
		{
			return null;
		}

		@Override
		public V putIfAbsent(final K key, final V value)
		{
			return null;
		}

		@Override
		public void putAll(final Map<? extends K, ? extends V> m)
		{
			// does nothing so it stays empty
		}

		@Override
		public boolean containsKey(final K key)
		{
			return false;
		}

		@Override
		public ImmutableMap<K, V> copy()
		{
			return ImmutableMap.of();
		}

		@Override
		public V get(final K key)
		{
			return null;
		}
	}

	private static final UpdatableMap<?, ?> EMPTY_MAP = new EmptyMap<>();

	/**
	 * Get instance of UpdatableMap that does not update anything and {@link UpdatableMap#copy()} always return an empty Map. This implementation does not raise any exception on
	 * {@link UpdatableMap#put(Object, Object)} and {@link UpdatableMap#putAll(Map)} method but merely return false always. This is useful merely for polymorphism.
	 * 
	 * @return "empty" Set, i.e. list that silently ignores updates and always stays empty
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> UpdatableMap<K, V> emptyMap()
	{
		return (UpdatableMap<K, V>) EMPTY_MAP;
	}

	/**
	 * {@link HashObjObjMap}-based implementation of {@link UpdatableMap}
	 */
	private static final class UpdatableHashMap<K, V> implements UpdatableMap<K, V>
	{
		private final Map<K, V> map = HashCollections.newUpdatableMap();

		private UpdatableHashMap()
		{
			// Map initialized above
		}

		@Override
		public V put(final K key, final V value)
		{
			return map.put(key, value);
		}

		@Override
		public V putIfAbsent(final K key, final V value)
		{
			return map.putIfAbsent(key, value);
		}

		@Override
		public void putAll(final Map<? extends K, ? extends V> m)
		{
			map.putAll(m);
		}

		@Override
		public boolean containsKey(final K key)
		{
			return map.containsKey(key);
		}

		@Override
		public ImmutableMap<K, V> copy()
		{
			return ImmutableMap.copyOf(map);
		}

		@Override
		public V get(final K key)
		{
			return map.get(key);
		}

	}

	/**
	 * Create new instance of UpdatableMap not accepting null values
	 * 
	 * @return new instance
	 */
	public static <K, V> UpdatableMap<K, V> newUpdatableMap()
	{
		return new UpdatableHashMap<>();
	}

}