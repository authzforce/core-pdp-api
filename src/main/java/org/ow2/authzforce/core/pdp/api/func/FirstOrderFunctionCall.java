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

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Value;

import java.util.Optional;

/**
 * First-order function call. It is the recommended way of calling any {@link FirstOrderFunction} instance.
 * <p>
 * Some arguments (expressions) may not be known in advance, but only at evaluation time (when calling {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}). For example, when using
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
	 * Make the call in a given evaluation context and argument values resolved at evaluation time. This method is called by {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)} after
	 * checking evaluation-time args.
	 * 
	 * @param context
	 *           Individual Decision evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used
	 * @param remainingArgs
	 *            remaining args (not yet known at initialization time). Null if none. Only non-bag/primitive values are valid <code>remainingArgs</code> to prevent varargs warning (potential heap pollution via varargs parameter) that would be caused by using a parameterized type such as Value/Collection
	 *            to represent both bags and primitives.
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if any error evaluating the function
	 */
	RETURN evaluate(EvaluationContext context, Optional<EvaluationContext> mdpContext, AttributeValue... remainingArgs) throws IndeterminateEvaluationException;

	/**
	 * Make the call in a given evaluation context. This method calls the function after checking <code>remainingArgTypes</code> if <code>checkremainingArgTypes = true</code>
	 * 
	 * @param context
	 *            Individual Decision evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used
	 * @param checkRemainingArgTypes
	 *            whether to check types of <code>remainingArgs</code>. It is strongly recommended setting this to <code>true</code> always, unless you have already checked the types are OK before
	 *            calling this method and want to skip re-checking for efficiency.
	 * 
	 * @param remainingArgs
	 *            remaining args.
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if <code>checkremainingArgTypes = true</code> and <code>remainingArgs</code> do not check OK, or if they do but there was an error evaluating the function with such arguments
	 */
	RETURN evaluate(EvaluationContext context, Optional<EvaluationContext> mdpContext, boolean checkRemainingArgTypes, AttributeValue... remainingArgs) throws IndeterminateEvaluationException;

}