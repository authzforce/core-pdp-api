/**
 * Copyright 2012-2019 THALES.
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
	 * post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compateTo() function is similar to {@link Comparable#compareTo(Object)}.
	 *
	 */
	public enum PostCondition
	{

		/**
		 * 
		 */
		GREATER_THAN("-greater-than", new Checker()
		{
			@Override
			public boolean check(final int comparisonResult)
			{
				return comparisonResult > 0;
			}
		}),
		/**
		 * 
		 */
		GREATER_THAN_OR_EQUAL("-greater-than-or-equal", new Checker()
		{
			@Override
			public boolean check(final int comparisonResult)
			{
				return comparisonResult >= 0;
			}
		}),
		/**
		 * 
		 */
		LESS_THAN("-less-than", new Checker()
		{
			@Override
			public boolean check(final int comparisonResult)
			{
				return comparisonResult < 0;
			}
		}),
		/**
		 * 
		 */
		LESS_THAN_OR_EQUAL("-less-than-or-equal", new Checker()
		{
			@Override
			public boolean check(final int comparisonResult)
			{
				return comparisonResult <= 0;
			}
		});

		private final String functionSuffix;
		private final Checker checker;

		private PostCondition(final String funcSuffix, final Checker checker)
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

	private static final class CallFactory<V extends AttributeValue & Comparable<V>>
	{
		private final PostCondition postCondition;
		private final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, V> funcSig;
		private final String illegalComparisonMsgPrefix;

		/**
		 * Creates comparison function call factory
		 * 
		 * @param condition
		 *            post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compateTo() function is similar to {@link Comparable#compareTo(Object)}.
		 */
		private CallFactory(final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, V> functionSig, final PostCondition postCondition)
		{
			this.funcSig = functionSig;
			this.postCondition = postCondition;
			illegalComparisonMsgPrefix = "Function " + funcSig.getName() + ": cannot compare arguments: ";
		}

		private FirstOrderFunctionCall<BooleanValue> getInstance(final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<BooleanValue, V>(funcSig, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(final Deque<V> args) throws IndeterminateEvaluationException
				{
					// Now that we have real values, perform the comparison operation
					final V arg0 = args.poll();
					final V arg1 = args.poll();
					final int comparResult;
					try
					{
						comparResult = arg0.compareTo(arg1);
					}
					catch (final IllegalArgumentException e)
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

	private final CallFactory<AV> funcCallFactory;

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
	 *            post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compateTo() function is similar to {@link Comparable#compareTo(Object)}
	 * 
	 * @throws IllegalArgumentException
	 *             if the function is unknown
	 */
	public ComparisonFunction(final Datatype<AV> paramType, final PostCondition postCondition)
	{
		super(paramType.getFunctionIdPrefix() + postCondition.functionSuffix, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, postCondition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}
}
