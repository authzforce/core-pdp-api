/*
 * Copyright 2012-2022 THALES.
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
package org.ow2.authzforce.core.pdp.api.combining;

import org.ow2.authzforce.core.pdp.api.Decidable;

import com.google.common.collect.ImmutableList;

/**
 * This class provides a skeletal implementation of the {@link CombiningAlg} interface to minimize the effort required to implement this interface.
 * 
 * @param <T>
 *            type of combined element (Policy, Rule...)
 */
public abstract class BaseCombiningAlg<T extends Decidable> implements CombiningAlg<T>
{
	// the identifier for the algorithm
	private final String id;

	private final Class<T> combinedElementType;

	private transient final String toString;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the algorithm's id
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI for
	 *            example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param combinedType
	 *            combined element type
	 */
	public BaseCombiningAlg(final String id, final Class<T> combinedType)
	{
		this.combinedElementType = combinedType;
		this.id = id;
		this.toString = "Combining Algorithm '" + id + "'";
	}

	@Override
	public final String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return this.toString;
	}

	/**
	 * Get to know whether this is a policy/policySet or rule-combining algorithm
	 * 
	 * @return the combinedElementType
	 */
	@Override
	public final Class<T> getCombinedElementType()
	{
		return combinedElementType;
	}

	/**
	 * This class provides a skeletal implementation of the {@link CombiningAlg.Evaluator} interface to minimize the effort required to implement this interface.
	 * 
	 * @param <T>
	 *            type of combined element
	 */
	public static abstract class Evaluator<T extends Decidable> implements CombiningAlg.Evaluator
	{
		private final ImmutableList<T> combinedElements;

		/**
		 * Creates instance
		 * 
		 * @param combinedElements
		 *            combined elements
		 */
		public Evaluator(final Iterable<? extends T> combinedElements)
		{
			this.combinedElements = ImmutableList.copyOf(combinedElements);
		}

		/**
		 * Get combined elements
		 * 
		 * @return combined elements
		 */
		public Iterable<T> getCombinedElements()
		{
			return combinedElements;
		}
	}

}
