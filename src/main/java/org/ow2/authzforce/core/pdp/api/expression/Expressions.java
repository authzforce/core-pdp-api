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
package org.ow2.authzforce.core.pdp.api.expression;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class consists exclusively of constants and static methods to operate on {@link Expression}s.
 * 
 */
public final class Expressions
{

	private static final Logger LOGGER = LoggerFactory.getLogger(Expressions.class);
	private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException(
			"No value returned by arg evaluation in the current context", StatusHelper.STATUS_PROCESSING_ERROR);
	private static final IndeterminateEvaluationException NULL_EXPECTED_RETURN_TYPE_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException("Undefined expected attribute datatype",
			StatusHelper.STATUS_SYNTAX_ERROR);

	/**
	 * Evaluate single-valued (primitive) argument expression
	 * 
	 * @param arg
	 *            argument expression
	 * @param context
	 *            context in which argument expression is evaluated
	 * @param returnType
	 *            type of returned attribute value
	 * @return result of evaluation
	 * @throws IndeterminateEvaluationException
	 *             if no value returned from evaluation, or <code>returnType</code> is not a supertype of the result value datatype
	 */
	public static <V extends Value> V eval(final Expression<?> arg, final EvaluationContext context, final Datatype<V> returnType) throws IndeterminateEvaluationException
	{
		if (returnType == null)
		{
			throw NULL_EXPECTED_RETURN_TYPE_INDETERMINATE_EXCEPTION;
		}

		final Value val = arg.evaluate(context);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("eval( arg = <{}>, <context>, expectedType = <{}> ) -> <{}>", arg, returnType, val);
		}

		if (val == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return returnType.cast(val);
		}
		catch (final ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid expression evaluation result type: " + arg.getReturnType() + ". Expected: " + returnType, StatusHelper.STATUS_PROCESSING_ERROR, e);
		}
	}

	/**
	 * Evaluate single-valued (primitive) argument expression
	 * 
	 * @param arg
	 *            argument expression
	 * @param context
	 *            context in which argument expression is evaluated
	 * @return result of evaluation
	 * @throws IndeterminateEvaluationException
	 *             if no value returned from evaluation, or <code>returnType</code> is not a supertype of the result value datatype
	 */
	public static AttributeValue evalPrimitive(final Expression<?> arg, final EvaluationContext context) throws IndeterminateEvaluationException
	{
		final Value val = arg.evaluate(context);
		if (LOGGER.isDebugEnabled())
		{
			/*
			 * Findsecbugs: prevent CRLF log injection
			 */
			LOGGER.debug("evalPrimitive( arg = <{}>, <context>) -> <{}>", arg, val);
		}

		if (val == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return (AttributeValue) val;
		}
		catch (final ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid expression evaluation result type: " + arg.getReturnType() + ". Expected: any primitive type", StatusHelper.STATUS_PROCESSING_ERROR, e);
		}
	}

	private Expressions()
	{
		// prevent instantiation
	}
}