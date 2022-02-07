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
package org.ow2.authzforce.core.pdp.api.func;

import com.google.common.collect.ImmutableList;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * First-order function signature whose parameters have (at least two) different datatypes
 * 
 * @param <RETURN>
 *            function's return type
 */
public class MultiParameterTypedFirstOrderFunctionSignature<RETURN extends Value> extends FirstOrderFunctionSignature<RETURN>
{

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	private final ImmutableList<? extends Datatype<?>> paramTypes;

	/**
	 * 
	 * @param name function name
	 * @param returnType function's return type
	 * @param varArgs true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}, i.e. the final type in <code>paramTypes</code> can be repeated 0 or more times to
	 * 	 *            match a variable-length argument
	 * 	 *            <p>
	 * 	 *            Examples with varargs=true ('...' means varargs like in Java):
	 * 	 *            </p>
	 * 	 *            <p>
	 * 	 *            Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}
	 * 	 *            </p>
	 * 	 *            <p>
	 * 	 *            Example 2: or(boolean...) -> paramTypes={boolean} (As you can see, isVarargs=true really means 0 or more args; indeed, the or function can take 0 parameter according to spec)
	 * 	 *            </p>
	 * 	 *            <p>
	 * 	 *            Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}
	 * 	 *            </p>
	 * @param parameterTypes function's parameter types
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
		this.paramTypes = ImmutableList.copyOf(parameterTypes);
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