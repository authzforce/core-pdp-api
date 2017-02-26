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

import java.lang.reflect.Array;

import com.google.common.reflect.TypeToken;

/**
 * Base class for datatype-specific Attribute Value Factory.
 * 
 * @param <INSTANCE_AV>
 *            type of instance (attribute values) created by this factory
 */
public abstract class BaseDatatypeFactory<INSTANCE_AV extends AttributeValue> implements DatatypeFactory<INSTANCE_AV>
{
	protected final Datatype<INSTANCE_AV> instanceDatatype;
	private final Bag<INSTANCE_AV> emptyBag;
	private final BagDatatype<INSTANCE_AV> bagDatatype;
	private final Class<INSTANCE_AV[]> arrayClass;

	// cached method results
	private final transient int hashCode;
	private final transient String toString;

	/**
	 * Base datatype factory constructor
	 * 
	 * @param instanceClass
	 *            (non-null) Java class used as implementation for the expression datatype
	 * @param datatypeId
	 *            (non-null) datatype ID
	 * @param functionIdPrefix
	 *            (non-null) prefix of ID of any standard generic (e.g. bag/set) function built on this datatype, e.g. 'urn:oasis:names:tc:xacml:1.0:function:string' for string datatype
	 * @throws NullPointerException
	 *             if {@code instanceClass == null || datatypeId == null || functionIdPrefix == null}.
	 */
	protected BaseDatatypeFactory(final Class<INSTANCE_AV> instanceClass, final String datatypeId, final String functionIdPrefix) throws NullPointerException
	{
		this.instanceDatatype = new PrimitiveDatatype<>(instanceClass, datatypeId, functionIdPrefix);
		this.emptyBag = Bags.empty(instanceDatatype, null);
		this.bagDatatype = new BagDatatype<>(new TypeToken<Bag<INSTANCE_AV>>()
		{
			private static final long serialVersionUID = 1L;
		}, instanceDatatype);
		this.arrayClass = (Class<INSTANCE_AV[]>) Array.newInstance(instanceClass, 0).getClass();

		this.toString = getClass().getName() + "[datatype=" + instanceDatatype + "]";
		this.hashCode = instanceDatatype.hashCode();
	}

	@Override
	public final String getId()
	{
		return this.instanceDatatype.getId();
	}

	@Override
	public final Datatype<INSTANCE_AV> getDatatype()
	{
		return instanceDatatype;
	}

	@Override
	public final Bag<INSTANCE_AV> getEmptyBag()
	{
		return emptyBag;
	}

	@Override
	public final BagDatatype<INSTANCE_AV> getBagDatatype()
	{
		return bagDatatype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.value.DatatypeFactory#getArrayClass()
	 */
	@Override
	public Class<INSTANCE_AV[]> getArrayClass()
	{
		return this.arrayClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString()
	{
		return toString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode()
	{
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof BaseDatatypeFactory))
		{
			return false;
		}

		final DatatypeFactory<?> other = (DatatypeFactory<?>) obj;
		/*
		 * if (instanceClass == null) { if (other.instanceClass != null) { return false; } } else
		 */
		return instanceDatatype.equals(other.getDatatype());
	}
}