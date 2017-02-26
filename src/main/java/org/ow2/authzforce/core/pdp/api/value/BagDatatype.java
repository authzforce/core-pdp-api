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
import java.util.Objects;
import java.util.Optional;

import com.google.common.reflect.TypeToken;

/**
 * Bag datatype for bags of primitive datatypes
 * 
 * @param <AV>
 *            Java type implementing this datatype (instance type)
 */
public final class BagDatatype<AV extends AttributeValue> extends Datatype<Bag<AV>>
{

	private static final ClassCastException DEFAULT_CLASS_CAST_EXCEPTION = new ClassCastException("Input is not a Bag");

	/**
	 * Bag datatype ID prefix, for internal identification purposes. This is an invalid URI on purpose, to avoid conflict with any custom XACML datatype URI (datatype extension).
	 */
	private static final String ID_PREFIX = "bag";

	private final Optional<Datatype<?>> genericTypeParam;
	private final Datatype<AV> elementDatatype;

	/**
	 * Default constructor
	 * 
	 * @throws NullPointerException
	 *             if {@code genericBagType == null || elementDatatype == null}.
	 */
	BagDatatype(final TypeToken<Bag<AV>> genericBagType, final Datatype<AV> elementDatatype) throws NullPointerException
	{
		super(genericBagType, Optional.of(Objects.requireNonNull(elementDatatype)), ID_PREFIX + "<" + elementDatatype + ">", elementDatatype.getFunctionIdPrefix() + "-" + ID_PREFIX);
		this.elementDatatype = Objects.requireNonNull(elementDatatype, "Undefined typeParam");
		this.genericTypeParam = Optional.of(this.elementDatatype);
	}

	@Override
	public boolean isInstance(final Value val)
	{
		return val instanceof Bag && elementDatatype.equals(((Bag<?>) val).getElementDatatype());
	}

	@Override
	public Bag<AV> cast(final Value val) throws ClassCastException
	{
		if (val instanceof Bag)
		{
			return (Bag<AV>) val;
		}

		throw DEFAULT_CLASS_CAST_EXCEPTION;
	}

	@Override
	public Optional<Datatype<?>> getTypeParameter()
	{
		return this.genericTypeParam;
	}

	@Override
	public Bag<AV>[] newArray(final int length)
	{
		return (Bag<AV>[]) Array.newInstance(Bag.class, length);
	}

	/**
	 * Returns the bag element datatype (datatype of every element in a bag of this datatype). Same as {@link #getTypeParameter()}.
	 * 
	 * @return bag element datatype
	 */
	public Datatype<AV> getElementType()
	{
		return this.elementDatatype;
	}

}