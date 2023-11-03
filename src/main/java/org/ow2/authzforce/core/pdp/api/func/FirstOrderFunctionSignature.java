/*
 * Copyright 2012-2023 THALES.
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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * First-order function signature (name, return type, arity, parameter types)
 * 
 * @param <RETURN_T>
 *            function's return type
 */
public abstract class FirstOrderFunctionSignature<RETURN_T extends Value>
{
	private static final IllegalArgumentException NULL_NAME_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined function name arg");
	private static final IllegalArgumentException NULL_RETURN_TYPE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined function return type arg");

	/**
	 * Exception to be thrown if one of the function parameter types is undefined
	 */
	protected static final IllegalArgumentException UNDEF_PARAMETER_TYPES_EXCEPTION = new IllegalArgumentException("Undefined function parameter types");

	/**
	 * 	function ID
 	 */
	protected final String name;

	/**
	 * The return type of the function
	 */
	protected final Datatype<RETURN_T> returnType;

	/**
	 * Is the last parameter specified in <code>paramTypes</code> considered as variable-length (like Java {@link Method#isVarArgs()}), i.e. taking a variable number of arguments (0 or more) of the
	 * specified paramTypes[n-1] with n the size of paramTypes. In the following examples, '...' means varargs like in Java:
	 * <p>
	 * Example 1: string-concat(string, string, string...) -> paramTypes={string, string, string}, isVarargs=true
	 * </p>
	 * <p>
	 * Example 2: or(boolean...) -> paramTypes={boolean}, isVarargs=true (As you can see, isVarargs=true really means 0 or more args; indeed, the or function can take 0 parameter according to spec)
	 * </p>
	 * <p>
	 * Example 3: n-of(integer, boolean...) -> paramTypes={integer, boolean}, isVarargs=true
	 * </p>
	 * <p>
	 * Example 4: abs(integer) -> paramTypes={integer}, isVarargs=false
	 * </p>
	 * <p>
	 * Example 5: string-equal(string, string) -> paramTypes={string, string}, isVarargs=false
	 * </p>
	 * <p>
	 * Example 6: date-add-yearMonthDuration(date, yearMonthDuration) -> paramTypes={date, yearMonthDuration}, isVarargs=false
	 * </p>
	 */
	protected final boolean isVarArgs;

	// cached method results
	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * Creates function signature
	 * 
	 * @param name
	 *            function name (e.g. XACML-defined URI)
	 * 
	 * @param returnType
	 *            function's return type
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
	 *             if ( {@code name == null || returnType == null })
	 */
	protected FirstOrderFunctionSignature(final String name, final Datatype<RETURN_T> returnType, final boolean varArgs) throws IllegalArgumentException
	{
		if (name == null)
		{
			throw NULL_NAME_ARGUMENT_EXCEPTION;
		}

		if (returnType == null)
		{
			throw NULL_RETURN_TYPE_ARGUMENT_EXCEPTION;
		}

		this.name = name;
		this.returnType = returnType;
		this.isVarArgs = varArgs;
	}

	/**
	 * Get function name
	 * 
	 * @return function name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get function return type
	 * 
	 * @return function return type
	 */
	public Datatype<RETURN_T> getReturnType()
	{
		return returnType;
	}

	/**
	 * Returns {@code true} if this method was declared to take a variable number of arguments; returns {@code false} otherwise.
	 * 
	 * @return {@code true} iff this method was declared to take a variable number of arguments.
	 */
	public boolean isVarArgs()
	{
		return isVarArgs;
	}

	/**
	 * Get function parameter types
	 * 
	 * @return function parameter types
	 */
	public abstract List<? extends Datatype<?>> getParameterTypes();

	@Override
	public String toString()
	{
		// immutable class -> cache result
		if (toString == null)
		{
			final StringBuilder strBuilder = new StringBuilder(returnType + " " + name + "(");
			final Iterator<? extends Datatype<?>> paramTypesIterator = this.getParameterTypes().iterator();
			// at least one parameter, we make sure of that in the constructor
			strBuilder.append(paramTypesIterator.next());
			while (paramTypesIterator.hasNext())
			{
				strBuilder.append(',').append(paramTypesIterator.next());
			}

			if (isVarArgs)
			{
				strBuilder.append("...");
			}

			strBuilder.append(')');
			toString = strBuilder.toString();
		}

		return toString;
	}
}
