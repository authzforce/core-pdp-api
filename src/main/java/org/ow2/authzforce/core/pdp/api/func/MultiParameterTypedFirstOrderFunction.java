/*
 * Copyright 2012-2021 THALES.
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
import java.util.List;

import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Superclass of "first-order" functions of multi-type parameters, i.e. whose parameters have different datatypes (at least two different). Supplies several useful methods, making it easier to
 * implement such "first-order" function.
 * 
 * @param <RETURN_T>
 *            function return type
 */
public abstract class MultiParameterTypedFirstOrderFunction<RETURN_T extends Value> extends FirstOrderFunction<RETURN_T>
{
	protected final FirstOrderFunctionSignature<RETURN_T> functionSignature;

	/**
	 * Constructor that creates a function from its signature definition
	 * 
	 * @param name
	 *            function name
	 * @param returnType
	 *            function return type
	 * @param varargs
	 *            true iff the function takes a variable number of arguments (like Java {@link Method#isVarArgs()}
	 * @param parameterTypes
	 *            function parameter types
	 * @throws IllegalArgumentException
	 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 2 })
	 * 
	 */
	public MultiParameterTypedFirstOrderFunction(final String name, final Datatype<RETURN_T> returnType, final boolean varargs, final List<? extends Datatype<?>> parameterTypes)
	{
		super(name);
		this.functionSignature = new MultiParameterTypedFirstOrderFunctionSignature<>(name, returnType, varargs, parameterTypes);
	}

	@Override
	public final Datatype<RETURN_T> getReturnType()
	{
		return functionSignature.getReturnType();
	}

	/**
	 * Get parameter types
	 * 
	 * @return parameter types
	 */
	@Override
	public final List<? extends Datatype<?>> getParameterTypes()
	{
		return functionSignature.getParameterTypes();
	}
}