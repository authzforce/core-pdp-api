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

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * First-order function call. It is the recommended way of calling any {@link FirstOrderFunction} instance.
 * <p>
 * Some of the arguments (expressions) may not be known in advance, but only at evaluation time (when calling {@link #evaluate(EvaluationContext, boolean, AttributeValue...)}). For example, when using
 * a FirstOrderFunction as a sub-function of the Higher-Order function 'any-of', the last arguments of the sub-function are determined during evaluation, after evaluating the expression of the last
 * input in the context, and getting the various values in the result bag.
 *
 * 
 * @param <RETURN>
 *            function return type
 * 
 * 
 */
public interface FirstOrderFunctionCall<RETURN extends Value> extends FunctionCall<RETURN>
{

	/**
	 * Make the call in a given evaluation context and argument values resolved at evaluation time. This method is called by {@link #evaluate(EvaluationContext, boolean, AttributeValue...)} after
	 * checking evaluation-time args.
	 * 
	 * @param context
	 *            evaluation context
	 * @param remainingArgs
	 *            remaining args (not yet known at initialization time). Null if none. Only non-bag/primitive values are valid <code>remainingArgs</code> to prevent varargs warning in
	 *            {@link #evaluate(EvaluationContext, AttributeValue...)} (potential heap pollution via varargs parameter) that would be caused by using a parameterized type such as Value/Collection
	 *            to represent both bags and primitives.
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if any error evaluating the function
	 */
	RETURN evaluate(EvaluationContext context, AttributeValue... remainingArgs) throws IndeterminateEvaluationException;

	/**
	 * Make the call in a given evaluation context. This method calls the function after checking <code>remainingArgTypes</code> if <code>checkremainingArgTypes = true</code>
	 * 
	 * @param context
	 *            evaluation context
	 * @param checkRemainingArgTypes
	 *            whether to check types of <code>remainingArgs</code>. It is strongly recommended to set this to <code>true</code> always, unless you have already checked the types are OK before
	 *            calling this method and want to skip re-checking for efficiency.
	 * 
	 * @param remainingArgs
	 *            remaining args.
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if <code>checkremainingArgTypes = true</code> and <code>remainingArgs</code> do not check OK, or if they do but there was an error evaluating the function with such arguments
	 */
	RETURN evaluate(EvaluationContext context, boolean checkRemainingArgTypes, AttributeValue... remainingArgs) throws IndeterminateEvaluationException;

}