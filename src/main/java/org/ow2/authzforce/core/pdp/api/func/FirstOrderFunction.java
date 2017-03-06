/**
 * Copyright 2012-2017 Thales Services SAS.
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
public abstract class FirstOrderFunction<RETURN extends Value> extends BaseFunction<RETURN>
{
	private static final Datatype<?>[] EMPTY_DATATYPE_DEF_ARRAY = new Datatype<?>[] {};

	protected FirstOrderFunction(final String name)
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
	 *            {@link BaseFirstOrderFunctionCall#evaluate(org.ow2.authzforce.core.pdp.api.EvaluationContext, boolean, org.ow2.authzforce.core.pdp.api.value.AttributeValue...)} at request evaluation
	 *            time, via the returned <code>FunctionCall</code>.
	 * @return Function call handle for calling this function which such inputs (with possible changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	public abstract FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException;

	@Override
	public final FunctionCall<RETURN> newCall(final List<Expression<?>> argExpressions) throws IllegalArgumentException
	{
		return newCall(argExpressions, EMPTY_DATATYPE_DEF_ARRAY);
	}
}