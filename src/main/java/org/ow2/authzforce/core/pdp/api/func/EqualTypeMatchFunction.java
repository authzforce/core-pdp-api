/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.api.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 * Generic match function taking parameters of same/equal type, like standard (A.3.1) Equality predicates and special match function x500Name-match
 *
 * @param <PARAM>
 *            type of compared parameters
 * 
 * @version $Id: $
 */
public class EqualTypeMatchFunction<PARAM extends AttributeValue> extends FirstOrderFunction.SingleParameterTyped<BooleanValue, PARAM>
{

	/**
	 * Equal-type match function call factory
	 *
	 * @param <PARAM_T>
	 *            match function parameter
	 */
	public static class CallFactory<PARAM_T extends AttributeValue>
	{

		private final FunctionSignature.SingleParameterTyped<BooleanValue, PARAM_T> funcSig;
		private final Matcher<PARAM_T> matcher;

		protected CallFactory(FunctionSignature.SingleParameterTyped<BooleanValue, PARAM_T> functionSignature, Matcher<PARAM_T> matcher)
		{
			this.funcSig = functionSignature;
			this.matcher = matcher;
		}

		protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<BooleanValue, PARAM_T>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected final BooleanValue evaluate(Deque<PARAM_T> args) throws IndeterminateEvaluationException
				{
					return BooleanValue.valueOf(matcher.match(args.poll(), args.poll()));
				}

			};
		}

	}

	/**
	 * Equal-type match function call factory builder
	 *
	 * @param <PARAM_T>
	 *            match function parameter type
	 */
	public interface CallFactoryBuilder<PARAM_T extends AttributeValue>
	{
		/**
		 * Builds the match function call factory
		 * 
		 * @param functionSignature
		 *            match function signature
		 * @return match function call factory
		 */
		CallFactory<PARAM_T> build(FunctionSignature.SingleParameterTyped<BooleanValue, PARAM_T> functionSignature);
	}

	/**
	 * Generic match method interface
	 *
	 * @param <PARAM_T>
	 *            type of arguments to be matched against each other
	 */
	public interface Matcher<PARAM_T extends AttributeValue>
	{
		/**
		 * Match two arguments (same type)
		 * 
		 * @param arg0
		 *            first argument
		 * @param arg1
		 *            second argument to be matched against the first one
		 * @return true iff arguments match (match algorithm is implementation-specific)
		 */
		boolean match(PARAM_T arg0, PARAM_T arg1);
	}

	private final CallFactory<PARAM> funcCallFactory;

	/**
	 * Constructor based on a match function
	 * 
	 * @param functionName
	 *            the standard XACML name of the function to be handled by this object, including the full namespace
	 * @param paramType
	 *            parameter type
	 * @param matcher
	 *            raw match method
	 */
	public EqualTypeMatchFunction(String functionName, Datatype<PARAM> paramType, Matcher<PARAM> matcher)
	{
		super(functionName, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, matcher);
	}

	/**
	 * Constructor based on a match method call factory builder
	 * 
	 * @param functionName
	 *            the standard XACML name of the function to be handled by this object, including the full namespace
	 * @param paramType
	 *            parameter type
	 * @param callFactoryBuilder
	 *            raw match method
	 */
	public EqualTypeMatchFunction(String functionName, Datatype<PARAM> paramType, CallFactoryBuilder<PARAM> callFactoryBuilder)
	{
		super(functionName, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = callFactoryBuilder.build(functionSignature);
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

	/**
	 * *-equal function matcher
	 * 
	 * @param <PARAM>
	 *            parameter type
	 */
	public static final class EqualMatcher<PARAM extends AttributeValue> implements Matcher<PARAM>
	{
		@Override
		public boolean match(PARAM arg0, PARAM arg1)
		{
			return arg0.equals(arg1);
		}
	}

	/**
	 * *-equal-ignore-case function matcher
	 * 
	 * @param <PARAM>
	 *            parameter type
	 */
	public static final class EqualIgnoreCaseMatcher<PARAM extends SimpleValue<String>> implements Matcher<PARAM>
	{
		@Override
		public boolean match(PARAM arg0, PARAM arg1)
		{
			return arg0.getUnderlyingValue().equalsIgnoreCase(arg1.getUnderlyingValue());
		}
	}

}
