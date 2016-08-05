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
package org.ow2.authzforce.core.pdp.api.func;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * First-order function signature whose parameters have (at least two) different datatypes
 * 
 * @param <RETURN>
 *            function's return type
 */
public class MultiParameterTypedFirstOrderFunctionSignature<RETURN extends Value> extends FirstOrderFunctionSignature<RETURN>
{

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	private final List<? extends Datatype<?>> paramTypes;

	/**
	 * 
	 * @param name
	 * @param returnType
	 * @param varArgs
	 * @param parameterTypes
	 * @throws IllegalArgumentException
	 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 2 })
	 */
	MultiParameterTypedFirstOrderFunctionSignature(final String name, final Datatype<RETURN> returnType, final boolean varArgs, final List<? extends Datatype<?>> parameterTypes)
			throws IllegalArgumentException
	{
		super(name, returnType, varArgs);
		if (parameterTypes == null)
		{
			throw UNDEF_PARAMETER_TYPES_EXCEPTION;
		}

		if (parameterTypes.size() < 2)
		{
			throw new IllegalArgumentException("Invalid number of function parameters (" + parameterTypes.size() + ") for multi-parameter-typed function (" + name + "). Required: >= " + 2 + ".");
		}
		this.paramTypes = Collections.unmodifiableList(parameterTypes);
	}

	/**
	 * Get function parameter types
	 * 
	 * @return function parameter types
	 */
	@Override
	public List<? extends Datatype<?>> getParameterTypes()
	{
		return this.paramTypes;
	}

	@Override
	public int hashCode()
	{
		// immutable class -> cache hashCode
		if (hashCode == 0)
		{
			hashCode = Objects.hash(name, returnType, isVarArgs, paramTypes);
		}

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

		if (!(obj instanceof MultiParameterTypedFirstOrderFunctionSignature))
		{
			return false;
		}

		final MultiParameterTypedFirstOrderFunctionSignature<?> other = (MultiParameterTypedFirstOrderFunctionSignature<?>) obj;
		return isVarArgs == other.isVarArgs && name.equals(other.name) && returnType.equals(other.returnType) && this.paramTypes.equals(other.paramTypes);
	}
}