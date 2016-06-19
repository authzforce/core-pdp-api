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
import java.util.List;

import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Superclass of "first-order" functions of single-type parameters, i.e. whose all parameters have the same datatype. Supplies several useful methods, making it easier to implement such
 * "first-order" function.
 * 
 * @param <RETURN_T>
 *            function return type
 * @param <PARAM_T>
 *            single/common parameter type
 */
public abstract class SingleParameterTypedFirstOrderFunction<RETURN_T extends Value, PARAM_T extends Value> extends FirstOrderFunction<RETURN_T>
{
	protected SingleParameterTypedFirstOrderFunctionSignature<RETURN_T, PARAM_T> functionSignature;

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
	 *            function parameter types. Note: the "? extends" allows to use {@link org.ow2.authzforce.core.pdp.api.value.BagDatatype}.
	 * @throws IllegalArgumentException
	 *             if ( {@code name == null || returnType == null || parameterTypes == null || parameterTypes.size() < 1 })
	 * 
	 */
	public SingleParameterTypedFirstOrderFunction(String name, Datatype<RETURN_T> returnType, boolean varargs, List<? extends Datatype<PARAM_T>> parameterTypes) throws IllegalArgumentException
	{
		super(name);
		this.functionSignature = new SingleParameterTypedFirstOrderFunctionSignature<>(name, returnType, varargs, parameterTypes);
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