/**
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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;

/**
 * Registry of extensions of specific type.
 * 
 * @param <T>
 *            type of extension in this registry
 */
public interface PdpExtensionRegistry<T extends PdpExtension>
{
	/**
	 * Get an extension by ID.
	 * 
	 * @param identity
	 *            ID of extension to look up
	 * 
	 * @return extension, null if none with such ID in the registry
	 */
	T getExtension(String identity);

	/**
	 * Get extensions
	 * 
	 * @return set of extensions currently registered
	 */
	Set<T> getExtensions();

	/**
	 * PDP extension comparator (compares IDs)
	 *
	 * @param <E>
	 *            extension type
	 */
	final class PdpExtensionComparator<E extends PdpExtension> implements Comparator<E>, Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(final E e1, final E e2)
		{
			return e1.getId().compareTo(e2.getId());
		}

	}

}
