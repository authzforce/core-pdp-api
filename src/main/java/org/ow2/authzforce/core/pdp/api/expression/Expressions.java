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
	private Expressions()
	{
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Expressions.class);
	private static final IndeterminateEvaluationException NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException(
			"No value returned by arg evaluation in the current context", StatusHelper.STATUS_PROCESSING_ERROR);
	private static final IndeterminateEvaluationException NULL_EXPECTED_RETURN_TYPE_INDETERMINATE_EXCEPTION = new IndeterminateEvaluationException(
			"Undefined expected attribute datatype", StatusHelper.STATUS_SYNTAX_ERROR);

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
	public static <V extends Value> V eval(Expression<?> arg, EvaluationContext context, Datatype<V> returnType) throws IndeterminateEvaluationException
	{
		if (returnType == null)
		{
			throw NULL_EXPECTED_RETURN_TYPE_INDETERMINATE_EXCEPTION;
		}

		final Value val = arg.evaluate(context);
		LOGGER.debug("eval( arg = <{}>, <context>, expectedType = <{}> ) -> <{}>", arg, returnType, val);
		if (val == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return returnType.cast(val);
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid expression evaluation result type: " + arg.getReturnType() + ". Expected: " + returnType,
					StatusHelper.STATUS_PROCESSING_ERROR, e);
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
	public static AttributeValue evalPrimitive(Expression<?> arg, EvaluationContext context) throws IndeterminateEvaluationException
	{
		final Value val = arg.evaluate(context);
		LOGGER.debug("evalPrimitive( arg = <{}>, <context>) -> <{}>", arg, val);
		if (val == null)
		{
			throw NULL_ARG_EVAL_RESULT_INDETERMINATE_EXCEPTION;
		}

		try
		{
			return (AttributeValue) val;
		} catch (ClassCastException e)
		{
			throw new IndeterminateEvaluationException("Invalid expression evaluation result type: " + arg.getReturnType() + ". Expected: any primitive type",
					StatusHelper.STATUS_PROCESSING_ERROR, e);
		}
	}
}