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
 * Expression evaluation return type
 * 
 * @param <V>
 *            Java value type, which is one of the following:
 */
public abstract class Datatype<V extends Value>
{
	private static final IllegalArgumentException NULL_VALUE_CLASS_EXCEPTION = new IllegalArgumentException("Undefined value (datatype implementation) class arg");
	private static final IllegalArgumentException NULL_ID_EXCEPTION = new IllegalArgumentException("Undefined datatype ID arg");
	private static final IllegalArgumentException NULL_FUNC_ID_PREFIX_EXCEPTION = new IllegalArgumentException("Undefined datatype-based function ID prefix arg");

	private final String id;
	private final Class<V> valueClass;
	private final String funcIdPrefix;

	/**
	 * Instantiates generic datatype, i.e. taking a datatype parameter, like Java Generics, but more like Java Collection since there is only one type parameter in this case.
	 * 
	 * @param valueClass
	 *            Java (implementation) class of values of this datatype
	 * @param id
	 *            datatype ID
	 * @param functionIdPrefix
	 *            prefix of ID of any standard generic (e.g. bag/set) function built on this datatype, e.g. 'urn:oasis:names:tc:xacml:1.0:function:string' for string datatype
	 * @throws IllegalArgumentException
	 *             if {@code valueClass == null || id == null || functionIdPrefix == null}.
	 */
	protected Datatype(final Class<V> valueClass, final String id, final URI functionIdPrefix) throws IllegalArgumentException
	{
		if (valueClass == null)
		{
			throw NULL_VALUE_CLASS_EXCEPTION;
		}

		if (id == null)
		{
			throw NULL_ID_EXCEPTION;
		}

		if (functionIdPrefix == null)
		{
			throw NULL_FUNC_ID_PREFIX_EXCEPTION;
		}

		this.valueClass = valueClass;
		this.id = id;
		this.funcIdPrefix = functionIdPrefix.toASCIIString();
	}

	/**
	 * Get value class, which is the Java (implementation) class of all instances of this datatype
	 * 
	 * @return value class
	 */
	public Class<V> getValueClass()
	{
		return valueClass;
	}

	/**
	 * Get ID (URI) of this datatype
	 * 
	 * @return datatype ID
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * Gets prefix of ID of any standard generic (e.g. bag/set) function built on this datatype, e.g. 'urn:oasis:names:tc:xacml:1.0:function:string' for string datatype
	 * 
	 * @return ID prefix for functions of this datatype
	 */
	public String getFuncIdPrefix()
	{
		return funcIdPrefix;
	}

	/**
	 * Return datatype of sub-elements for this datatype, e.g. the bag element datatype (datatype of every element in a bag of this datatype); null if this is a primitive type (no sub-elements)
	 * 
	 * @return datatype parameter, null for primitive datatypes
	 */
	public abstract Datatype<?> getTypeParameter();

	/**
	 * Casts a value to the class or interface represented by this datatype.
	 * 
	 * @param val
	 *            value to be cast
	 * @return the value after casting, or null if {@code val} is null
	 * @throws ClassCastException
	 *             if the value is not null and is not assignable to the type V.
	 */
	public V cast(final Value val) throws ClassCastException
	{
		return this.valueClass.cast(val);
	}

}