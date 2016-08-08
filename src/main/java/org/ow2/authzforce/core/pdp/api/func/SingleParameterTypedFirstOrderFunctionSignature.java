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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * First-order function signature whose every parameters has the same datatype
 * 
 * @param <RETURN>
 *            function's return type
 * @param <PARAM>
 *            common parameter type
 */
public class SingleParameterTypedFirstOrderFunctionSignature<RETURN extends Value, PARAM extends Value> extends FirstOrderFunctionSignature<RETURN>
{
	private transient volatile int hashCode = 0; // Effective Java - Item 9

	private final List<? extends Datatype<PARAM>> paramTypes;

	/**
	 * Creates function signature
	 * 
	 * @param name
	 *            function name (e.g. XACML-defined URI)
	 * 
	 * @param returnType
	 *            function's return type
	 * @param parameterTypes
	 *            function parameter types. Note: the "? extends" allows to use {@link BagDatatype} as parameterType
	 * @param varArgs
	 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}, i.e. the final type in <code>paramTypes</code> can be repeated 0 or more times to
	 *            match a variable-length argument
	 *            <p>
	 *            Examples with varargs=true ('...' means varargs like in Java):
	 *            </p>
	 *            <p>
	 *            Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}
	 *            </p>
	 *            <p>
	 *            Example 2: or(boolean...) -> paramTypes={boolean} (As you can see, isVarargs=true really means 0 or more args; indeed, the or function can take 0 parameter according to spec)
	 *            </p>
	 *            <p>
	 *            Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}
	 *            </p>
	 * @throws IllegalArgumentException
	 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.isEmpty()})
	 */
	public SingleParameterTypedFirstOrderFunctionSignature(final String name, final Datatype<RETURN> returnType, final boolean varArgs, final List<? extends Datatype<PARAM>> parameterTypes)
			throws IllegalArgumentException
	{
		super(name, returnType, varArgs);
		if (parameterTypes == null)
		{
			throw UNDEF_PARAMETER_TYPES_EXCEPTION;
		}

		if (parameterTypes.isEmpty())
		{
			throw new IllegalArgumentException("Invalid number of function parameters (" + parameterTypes.size() + ") for first-order function (" + name + "). Required: >= 1.");
		}

		this.paramTypes = Collections.unmodifiableList(parameterTypes);
	}

	/**
	 * Get single/common parameter datatype
	 * 
	 * @return parameter datatype
	 */
	public Datatype<PARAM> getParameterType()
	{
		// the constructor made sure that paramTypes is not empty
		return this.paramTypes.get(0);
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
			hashCode = Objects.hash(name, returnType, isVarArgs, paramTypes.get(0));
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

		if (!(obj instanceof SingleParameterTypedFirstOrderFunctionSignature))
		{
			return false;
		}

		final SingleParameterTypedFirstOrderFunctionSignature<?, ?> other = (SingleParameterTypedFirstOrderFunctionSignature<?, ?>) obj;
		return isVarArgs == other.isVarArgs && name.equals(other.name) && returnType.equals(other.returnType) && this.paramTypes.get(0).equals(other.paramTypes.get(0));
	}
}