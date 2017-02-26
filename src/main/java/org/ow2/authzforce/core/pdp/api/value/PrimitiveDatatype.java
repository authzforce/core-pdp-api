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
 * Primitive datatype
 *
 * @param <AV>
 *            value type
 */
public final class PrimitiveDatatype<AV extends AtomicValue> extends Datatype<AV>
{

	private static final ClassCastException DEFAULT_CLASS_CAST_EXCEPTION = new ClassCastException("Input is not a primitive value");
	private final Class<AV> valueClass;

	/**
	 * Default constructor
	 * 
	 * @throws NullPointerException
	 *             if {@code valueClass == null || id == null || functionIdPrefix == null}.
	 */
	PrimitiveDatatype(final Class<AV> valueClass, final String id, final String functionIdPrefix) throws NullPointerException
	{
		super(TypeToken.of(Objects.requireNonNull(valueClass, "Undefined valueClass arg")), Optional.empty(), id, functionIdPrefix);
		this.valueClass = valueClass;
	}

	@Override
	public boolean isInstance(final Value val)
	{
		return this.valueClass.isInstance(val);
	}

	@Override
	public AV cast(final Value val) throws ClassCastException
	{
		if (val instanceof AtomicValue)
		{
			return this.valueClass.cast(val);
		}

		throw DEFAULT_CLASS_CAST_EXCEPTION;
	}

	@Override
	public AV[] newArray(final int length)
	{
		return (AV[]) Array.newInstance(this.valueClass, length);
	}

	@Override
	public Optional<Datatype<?>> getTypeParameter()
	{
		return Optional.empty();
	}

}