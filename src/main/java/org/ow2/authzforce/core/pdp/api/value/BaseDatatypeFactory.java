/**
 * Copyright (C) 2012-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.api.value;

import java.lang.reflect.Array;
import java.net.URI;

/**
 * Base class for datatype-specific Attribute Value Factory.
 * 
 * @param <INSTANCE_AV>
 *            type of instance (attribute values) created by this factory
 */
public abstract class BaseDatatypeFactory<INSTANCE_AV extends AttributeValue> implements DatatypeFactory<INSTANCE_AV>
{
	/**
	 * Primitive datatype
	 *
	 * @param <AV>
	 *            attribute value type
	 */
	private static class PrimitiveDatatype<AV extends AttributeValue> extends Datatype<AV>
	{
		private final transient int hashCode;

		private PrimitiveDatatype(Class<AV> valueClass, String id, URI functionIdPrefix) throws IllegalArgumentException
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
		public boolean equals(Object obj)
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

	private static final IllegalArgumentException NULL_DATATYPE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined instanceClass argument");
	private static final IllegalArgumentException NULL_DATATYPE_ID_EXCEPTION = new IllegalArgumentException("Undefined datatypeId argument");

	protected final Datatype<INSTANCE_AV> instanceDatatype;
	private final Bag<INSTANCE_AV> emptyBag;
	private final BagDatatype<INSTANCE_AV> bagDatatype;
	private final Class<INSTANCE_AV[]> arrayClass;

	// cached method result
	private final transient int hashCode;
	private final transient String toString;

	protected BaseDatatypeFactory(Class<INSTANCE_AV> instanceClass, String datatypeId, URI functionIdPrefix)
	{
		if (instanceClass == null)
		{
			throw NULL_DATATYPE_CLASS_EXCEPTION;
		}

		if (datatypeId == null)
		{
			throw NULL_DATATYPE_ID_EXCEPTION;
		}

		this.instanceDatatype = new PrimitiveDatatype<>(instanceClass, datatypeId, functionIdPrefix);
		this.emptyBag = Bags.empty(instanceDatatype, null);
		this.bagDatatype = new BagDatatype<>(instanceDatatype);
		this.arrayClass = (Class<INSTANCE_AV[]>) Array.newInstance(instanceClass, 0).getClass();

		this.toString = getClass().getName() + "[datatype=" + instanceDatatype + "]";
		this.hashCode = instanceDatatype.getValueClass().hashCode();
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
	public final boolean equals(Object obj)
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