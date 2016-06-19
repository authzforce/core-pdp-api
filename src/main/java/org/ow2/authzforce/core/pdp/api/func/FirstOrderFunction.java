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

import java.util.List;

import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Superclass of "first-order" functions, "first-order" as opposed to "higher-order". (Higher-order functions are implemented in separate classes.) Supplies several useful methods, making it easier to
 * implement a "first-order" function.
 * 
 * @param <RETURN>
 *            function return type
 */
abstract class FirstOrderFunction<RETURN extends Value> extends BaseFunction<RETURN>
{
	private static final Datatype<?>[] EMPTY_DATATYPE_DEF_ARRAY = new Datatype<?>[] {};

	protected FirstOrderFunction(String name)
	{
		super(name);
	}

	/**
	 * Get parameter types
	 * 
	 * @return parameter types
	 */
	public abstract List<? extends Datatype<?>> getParameterTypes();

	/**
	 * Returns a function call for calling this function.
	 * 
	 * @param argExpressions
	 *            function arguments (expressions)
	 * 
	 * @param remainingArgTypes
	 *            types of remaining inputs to be passed only at request evaluation time, if not all arguments are specified in <code>argExpressions</code>. Therefore, only their type is checked at
	 *            this point. The actual argument values will be passed as last parameters when calling
	 *            {@link FirstOrderFunctionCall#evaluate(org.ow2.authzforce.core.pdp.api.EvaluationContext, boolean, org.ow2.authzforce.core.pdp.api.value.AttributeValue...)} at request evaluation
	 *            time, via the returned <code>FunctionCall</code>.
	 * @return Function call handle for calling this function which such inputs (with possible changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	public abstract FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException;

	@Override
	public final FunctionCall<RETURN> newCall(List<Expression<?>> argExpressions) throws IllegalArgumentException
	{
		return newCall(argExpressions, EMPTY_DATATYPE_DEF_ARRAY);
	}
}