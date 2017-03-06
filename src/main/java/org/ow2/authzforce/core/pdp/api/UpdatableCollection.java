/**
 * Copyright 2012-2017 Thales Services SAS.
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

import java.util.Collection;

import com.google.common.collect.ImmutableCollection;

/**
 * Updatable collection; "updatable" means elements can only be added to the
 * collection (no removal, no clear)
 * 
 * @param <E>
 *            the type of elements in this collection
 */
public interface UpdatableCollection<E>
{
	/**
	 * Append element to the collection
	 * 
	 * @param e
	 *            new element to append
	 * @return true iff the list changed as a result of the call
	 */
	boolean add(E e);

	/**
	 * Appends all the elements in the specified collection to this collection
	 * 
	 * @param c
	 *            list containing elements to be added to this list
	 * @return true iff the list changed as a result of the call
	 */
	boolean addAll(Collection<? extends E> c);

	/**
	 * Creates an immutable copy with all the elements currently in this
	 * collection
	 * 
	 * @return immutable copy of the collection
	 */
	ImmutableCollection<E> copy();
}
