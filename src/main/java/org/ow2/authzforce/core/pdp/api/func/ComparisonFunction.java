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
/**
 *
 */
package org.ow2.authzforce.core.pdp.api.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * A superclass of all the standard comparison functions (return a boolean). May be used for non-standard comparison functions as well.
 *
 * @param <AV>
 *            function parameter type
 * 
 * @version $Id: $
 */
public class ComparisonFunction<AV extends AttributeValue & Comparable<AV>> extends FirstOrderFunction.SingleParameterTyped<BooleanValue, AV>
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
			public boolean check(int comparisonResult)
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
			public boolean check(int comparisonResult)
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
			public boolean check(int comparisonResult)
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
			public boolean check(int comparisonResult)
			{
				return comparisonResult <= 0;
			}
		});

		private final String functionSuffix;
		private final Checker checker;

		private PostCondition(String funcSuffix, Checker checker)
		{
			this.functionSuffix = funcSuffix;
			this.checker = checker;
		}

		boolean isTrue(int comparisonResult)
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
		private final FunctionSignature.SingleParameterTyped<BooleanValue, V> funcSig;
		private final String illegalComparisonMsgPrefix;

		/**
		 * Creates comparison function call factory
		 * 
		 * @param condition
		 *            post-condition to hold true when comparing the result of <code>arg0.compareTo(arg1)</code> to zero; where compateTo() function is similar to {@link Comparable#compareTo(Object)}.
		 */
		private CallFactory(FunctionSignature.SingleParameterTyped<BooleanValue, V> functionSig, PostCondition postCondition)
		{
			this.funcSig = functionSig;
			this.postCondition = postCondition;
			illegalComparisonMsgPrefix = "Function " + funcSig.getName() + ": cannot compare arguments: ";
		}

		private FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<BooleanValue, V>(funcSig, argExpressions, remainingArgTypes)
			{

				@Override
				protected BooleanValue evaluate(Deque<V> args) throws IndeterminateEvaluationException
				{
					// Now that we have real values, perform the comparison operation
					final V arg0 = args.poll();
					final V arg1 = args.poll();
					final int comparResult;
					try
					{
						comparResult = arg0.compareTo(arg1);
					} catch (IllegalArgumentException e)
					{
						// See BaseTimeValue#compareTo() for example of comparison throwing such exception
						throw new IndeterminateEvaluationException(illegalComparisonMsgPrefix + arg0.getContent() + ", " + arg1.getContent(), StatusHelper.STATUS_PROCESSING_ERROR, e);
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
	public ComparisonFunction(Datatype<AV> paramType, PostCondition postCondition)
	{
		super(paramType.getFuncIdPrefix() + postCondition.functionSuffix, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, postCondition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thalesgroup.authzforce.core.func.FirstOrderFunction#getFunctionCall(java.util.List, com.thalesgroup.authzforce.core.eval.DatatypeDef[])
	 */
	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes)
	{
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}
}
