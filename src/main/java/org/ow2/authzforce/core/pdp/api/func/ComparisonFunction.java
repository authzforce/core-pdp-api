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
/**
 *
 */
package org.ow2.authzforce.core.pdp.api.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * A superclass of all the standard comparison functions (return a boolean). May be used for non-standard comparison functions as well.
 *
 * @param <AV>
 *            function parameter type
 * 
 * @version $Id: $
 */
public class ComparisonFunction<AV extends AttributeValue & Comparable<AV>> extends SingleParameterTypedFirstOrderFunction<BooleanValue, AV>
{
	/**
	 * post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compareTo() function is similar to {@link Comparable#compareTo(Object)}.
	 *
	 */
	public enum PostCondition
	{

		/**
		 * 
		 */
		GREATER_THAN("-greater-than", comparisonResult -> comparisonResult > 0),
		/**
		 * 
		 */
		GREATER_THAN_OR_EQUAL("-greater-than-or-equal", comparisonResult -> comparisonResult >= 0),
		/**
		 * 
		 */
		LESS_THAN("-less-than", comparisonResult -> comparisonResult < 0),
		/**
		 * 
		 */
		LESS_THAN_OR_EQUAL("-less-than-or-equal", comparisonResult -> comparisonResult <= 0);

		private final String functionSuffix;
		private final Checker checker;

		PostCondition(final String funcSuffix, final Checker checker)
		{
			this.functionSuffix = funcSuffix;
			this.checker = checker;
		}

		boolean isTrue(final int comparisonResult)
		{
			return checker.check(comparisonResult);
		}

		private interface Checker
		{
			boolean check(int comparisonResult);
		}
	}

	private final PostCondition postCondition;
	private final String illegalComparisonMsgPrefix;

	/**
	 * Creates a new comparison function. Resulting function ID = {@code paramType.getFuncIdPrefix() + functionSuffix}, where {@code functionSuffix} is:
	 * <ul>
	 * <li>If {@code postCondition == GREATER_THAN}, then the suffix is '-greater-than'</li>
	 * <li>If {@code postCondition == GREATER_THAN_OR_EQUAL}, then the suffix is '-greater-than-or-equal'</li>
	 * <li>If {@code postCondition == LESSER_THAN}, then the suffix is '-lesser-than'</li>
	 * <li>If {@code postCondition == LESSER_THAN_OR_EQUAL}, then the suffix is '-lesser-than-or-equal'</li>
	 * </ul>
	 * As a result, for example, if {@code paramType.getFuncIdPrefix()} is 'urn:oasis:names:tc:xacml:1.0:function:integer' and {@code postCondition == GREATER_THAN}, then the resulting function ID
	 * will be 'urn:oasis:names:tc:xacml:1.0:function:integer-greater-than'
	 * 
	 * @param paramType
	 *            parameter type
	 * @param postCondition
	 *            post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compareTo() function is similar to {@link Comparable#compareTo(Object)}
	 * 
	 * @throws IllegalArgumentException
	 *             if the function is unknown
	 */
	public ComparisonFunction(final Datatype<AV> paramType, final PostCondition postCondition)
	{
		super(paramType.getFunctionIdPrefix() + postCondition.functionSuffix, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramType, paramType));
		this.postCondition = postCondition;
		this.illegalComparisonMsgPrefix = "Function " + functionSignature.getName() + ": cannot compare arguments: ";
	}

	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	{
		//return funcCallFactory.getInstance(argExpressions, remainingArgTypes);

		return new EagerSinglePrimitiveTypeEval<>(functionSignature, argExpressions, remainingArgTypes)
		{

			@Override
			protected BooleanValue evaluate(final Deque<AV> args) throws IndeterminateEvaluationException
			{
				// Now that we have real values, perform the comparison operation
				final AV arg0 = args.poll();
				assert arg0 != null;
				final AV arg1 = args.poll();
				assert arg1 != null;
				final int comparResult;
				try
				{
					comparResult = arg0.compareTo(arg1);
				} catch (final IllegalArgumentException e)
				{
					// See BaseTimeValue#compareTo() for example of comparison throwing such exception
					throw new IndeterminateEvaluationException(illegalComparisonMsgPrefix + arg0.getContent() + ", " + arg1.getContent(), XacmlStatusCode.PROCESSING_ERROR.value(), e);
				}
				// Return the result as a BooleanAttributeValue.
				return BooleanValue.valueOf(postCondition.isTrue(comparResult));
			}
		};
	}
}
