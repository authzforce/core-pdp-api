/**
 * Copyright 2012-2020 THALES.
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
package org.ow2.authzforce.core.pdp.api;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Updatable Map; "updatable" means elements can only be put to the Map (no removal, no clear)
 * 
 * @param <K>
 *            type of keys in this map
 * @param <V>
 *            type of mapped values
 * 
 */
public interface UpdatableMap<K, V>
{
	/**
	 * Associates the specified value with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value. (A map m is said to
	 * contain a mapping for a key k if and only if m.containsKey(k) would return true.)
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key, if the implementation
	 *         supports null values.)
	 */
	V put(K key, V value);

	/**
	 * Associates the specified value with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value. (A map m is said to
	 * contain a mapping for a key k if and only if m.containsKey(k) would return true.)
	 * 
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            value to be associated with the specified key
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key, if the implementation
	 *         supports null values.)
	 */
	V putIfAbsent(K key, V value);

	/**
	 * Copies all of the mappings from the specified map to this map (optional operation). The effect of this call is equivalent to that of calling {@link #put(Object,Object) put(k, v)} on this map
	 * once for each mapping from key <tt>k</tt> to value <tt>v</tt> in the specified map. The behavior of this operation is undefined if the specified map is modified while the operation is in
	 * progress.
	 *
	 * @param m
	 *            mappings to be stored in this map
	 * @throws UnsupportedOperationException
	 *             if the <tt>putAll</tt> operation is not supported by this map
	 * @throws ClassCastException
	 *             if the class of a key or value in the specified map prevents it from being stored in this map
	 * @throws NullPointerException
	 *             if the specified map is null, or if this map does not permit null keys or values, and the specified map contains null keys or values
	 * @throws IllegalArgumentException
	 *             if some property of a key or value in the specified map prevents it from being stored in this map
	 */
	void putAll(Map<? extends K, ? extends V> m);

	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the specified key. More formally, returns <tt>true</tt> if and only if this map contains a mapping for a key <tt>k</tt> such that
	 * <tt>(key==null ? k==null : key.equals(k))</tt>. (There can be at most one such mapping.)
	 *
	 * @param key
	 *            key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified key
	 * @throws NullPointerException
	 *             if the specified key is null and this map does not permit null keys ( <a href="Collection.html#optional-restrictions">optional</a>)
	 */
	boolean containsKey(K key);

	/**
	 * Creates an immutable Map copy with all the elements currently in this Map
	 * 
	 * @return immutable copy of the Map
	 */
	ImmutableMap<K, V> copy();

	/**
	 * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise it returns {@code null}. (There can be at most one such mapping.)
	 *
	 * <p>
	 * If this map permits null values, then a return value of {@code null} does not <i>necessarily</i> indicate that the map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to {@code null}. The {@link #containsKey containsKey} operation may be used to distinguish these two cases.
	 *
	 * @param key
	 *            the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key
	 * @throws NullPointerException
	 *             if the specified key is null and this map does not permit null keys ( <a href="Collection.html#optional-restrictions">optional</a>)
	 */
	V get(K key);
}
