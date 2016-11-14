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
package org.ow2.authzforce.core.pdp.api.value;

import java.net.URI;

/**
 * Primitive datatype
 *
 * @param <AV>
 *            value type
 */
final class PrimitiveDatatype<AV extends AtomicValue> extends Datatype<AV>
{
	private final transient int hashCode;

	PrimitiveDatatype(final Class<AV> valueClass, final String id, final URI functionIdPrefix) throws IllegalArgumentException
	{
		super(valueClass, id, functionIdPrefix);
		// there should be one-to-one mapping between valueClass and id, so hashing
		// only one of these two is necessary
		hashCode = getValueClass().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof PrimitiveDatatype))
		{
			return false;
		}

		final PrimitiveDatatype<?> other = (PrimitiveDatatype<?>) obj;
		// there should be a one-to-one mapping between valueClass and id, so checking
		// only one of these two is necessary
		return this.getValueClass() == other.getValueClass();
	}

	@Override
	public Datatype<?> getTypeParameter()
	{
		return null;
	}
}