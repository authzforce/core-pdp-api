/*
 * Copyright 2012-2021 THALES.
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

import com.google.common.collect.ImmutableList;

/**
 * Updatable list; "updatable" means elements can only be added to the list (no removal, no clear)
 * 
 * @param <E>
 *            the type of elements in this list
 */
public interface UpdatableList<E> extends UpdatableCollection<E>
{

	/**
	 * Creates an immutable list copy with all the elements currently in this list
	 * 
	 * @return immutable copy of the list
	 */
	@Override
	ImmutableList<E> copy();
}
