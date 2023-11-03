/*
 * Copyright 2012-2023 THALES.
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
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.*;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Generic match function taking parameters of same/equal type, like standard
 * (A.3.1) Equality predicates and special match function x500Name-match
 *
 * @param <PARAM> type of compared parameters
 * 
 * @version $Id: $
 */
public class EqualTypeMatchFunction<PARAM extends AttributeValue>
		extends SingleParameterTypedFirstOrderFunction<BooleanValue, PARAM> {

	/**
	 * Equal-type match function call factory
	 *
	 * @param <PARAM_T> match function parameter
	 */
	public static class CallFactory<PARAM_T extends AttributeValue> {

		private final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, PARAM_T> funcSig;
		private final Matcher<PARAM_T> matcher;

		/**
		 * Constructor
		 * @param functionSignature call function signature
		 * @param matcher underlying match function
		 */
		protected CallFactory(
				final SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, PARAM_T> functionSignature,
				final Matcher<PARAM_T> matcher) {
			this.funcSig = functionSignature;
			this.matcher = matcher;
		}

		/**
		 * Creates function call instance
		 * @param argExpressions arguments (Expressions)
		 * @param remainingArgTypes types of arguments following <code>args</code>, and of which the actual Expression is unknown at this point, but will be known and passed at evaluation time as <code>remainingArgs</code> parameter to {@link EagerSinglePrimitiveTypeEval#evaluate(EvaluationContext, java.util.Optional, boolean, AttributeValue...)}, then {@link EagerSinglePrimitiveTypeEval#evaluate(EvaluationContext, java.util.Optional, AttributeValue...)}.
		 * @return call instance
		 * @throws IllegalArgumentException if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected FirstOrderFunctionCall<BooleanValue> getInstance(final List<Expression<?>> argExpressions,
				final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException {
			return new EagerSinglePrimitiveTypeEval<>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected BooleanValue evaluate(final Deque<PARAM_T> args)
				{
					return BooleanValue.valueOf(matcher.match(args.poll(), args.poll()));
				}

			};
		}

	}

	/**
	 * Equal-type match function call factory builder
	 *
	 * @param <PARAM_T> match function parameter type
	 */
	public interface CallFactoryBuilder<PARAM_T extends AttributeValue> {
		/**
		 * Builds the match function call factory
		 * 
		 * @param functionSignature match function signature
		 * @return match function call factory
		 */
		CallFactory<PARAM_T> build(
				SingleParameterTypedFirstOrderFunctionSignature<BooleanValue, PARAM_T> functionSignature);
	}

	/**
	 * Generic match method interface
	 *
	 * @param <PARAM_T> type of arguments to be matched against each other
	 */
	public interface Matcher<PARAM_T extends AttributeValue> {
		/**
		 * Match two arguments (same type)
		 * 
		 * @param arg0 first argument
		 * @param arg1 second argument to be matched against the first one
		 * @return true iff arguments match (match algorithm is implementation-specific)
		 */
		boolean match(PARAM_T arg0, PARAM_T arg1);
	}

	private final CallFactory<PARAM> funcCallFactory;

	/**
	 * Constructor based on a match function
	 * 
	 * @param functionName the standard XACML name of the function to be handled by
	 *                     this object, including the full namespace
	 * @param paramType    parameter type
	 * @param matcher      raw match method
	 */
	public EqualTypeMatchFunction(final String functionName, final Datatype<PARAM> paramType,
			final Matcher<PARAM> matcher) {
		super(functionName, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, matcher);
	}

	/**
	 * Constructor based on a match method call factory builder
	 * 
	 * @param functionName       the standard XACML name of the function to be
	 *                           handled by this object, including the full
	 *                           namespace
	 * @param paramType          parameter type
	 * @param callFactoryBuilder raw match method
	 */
	public EqualTypeMatchFunction(final String functionName, final Datatype<PARAM> paramType,
			final CallFactoryBuilder<PARAM> callFactoryBuilder) {
		super(functionName, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramType, paramType));
		this.funcCallFactory = callFactoryBuilder.build(functionSignature);
	}

	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions,
			final Datatype<?>... remainingArgTypes) {
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

	/**
	 * *-equal function matcher
	 * 
	 * @param <PARAM> parameter type
	 */
	public static final class EqualMatcher<PARAM extends AttributeValue> implements Matcher<PARAM> {
		@Override
		public boolean match(final PARAM arg0, final PARAM arg1) {
			return arg0.equals(arg1);
		}
	}

	/**
	 * *-equal-ignore-case function matcher
	 * 
	 * @param <PARAM> parameter type
	 */
	public static final class EqualIgnoreCaseMatcher<PARAM extends SimpleValue<String>> implements Matcher<PARAM> {
		@Override
		public boolean match(final PARAM arg0, final PARAM arg1) {
			final String normalized0 = arg0.getUnderlyingValue().toUpperCase(Locale.ENGLISH);
			final String normalized1 = arg1.getUnderlyingValue().toUpperCase(Locale.ENGLISH);
			return normalized0.equals(normalized1);
		}
	}

}
