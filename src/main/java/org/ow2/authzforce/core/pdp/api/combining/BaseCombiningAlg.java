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
package org.ow2.authzforce.core.pdp.api.combining;

import org.ow2.authzforce.core.pdp.api.Decidable;

/**
 * The base type for combining algorithms.
 * 
 * @param <T>
 *            type of combined element (Policy, Rule...)
 */
public abstract class BaseCombiningAlg<T extends Decidable> implements CombiningAlg<T>
{
	// the identifier for the algorithm
	private final String id;

	private final Class<T> combinedElementType;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the algorithm's id
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to XML schema anyURI type. Spaces are allowed in
	 *            XSD anyURI [1], not in java.net.URI for example. That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI
	 *            </p>
	 * @param combinedType
	 *            combined element type
	 */
	public BaseCombiningAlg(String id, Class<T> combinedType)
	{
		this.combinedElementType = combinedType;
		this.id = id;
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
		return this.id;
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
}