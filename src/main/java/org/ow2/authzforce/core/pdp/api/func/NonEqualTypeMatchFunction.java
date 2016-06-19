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

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerMultiPrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.SimpleValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;

/**
 * Generic match functions taking two parameters of possibly different types, e.g. a string and a URI.
 *
 * @param <T0>
 *            Type of the first parameter of this function.
 * @param <T1>
 *            Type of the second parameter of this function.
 * 
 * @version $Id: $
 */
public class NonEqualTypeMatchFunction<T0 extends AttributeValue, T1 extends AttributeValue> extends FirstOrderFunctions.MultiParameterTypedFirstOrderFunction<BooleanValue>
{
	/**
	 * Generic match method interface for values of different types
	 * 
	 * @param <T0>
	 *            first type of value to be matched
	 * @param <T1>
	 *            second type of value to be matched
	 *
	 */
	public interface Matcher<T0 extends AttributeValue, T1 extends AttributeValue>
	{
		/**
		 * Evaluate function with second parameter as string
		 * 
		 * @param arg0
		 *            first function parameter
		 * @param arg1
		 *            second function parameter
		 * @return true if and only if both arguments match according to the matcher definition
		 * @throws IllegalArgumentException
		 *             if one of the arguments is not valid for this matcher
		 */
		boolean match(T0 arg0, T1 arg1) throws IllegalArgumentException;
	}

	/**
	 * Match function call factory
	 * 
	 * @param <T0>
	 *            first parameter type of match function
	 * @param <T1>
	 *            second parameter type of match function
	 *
	 */
	public static class CallFactory<T0 extends AttributeValue, T1 extends AttributeValue>
	{
		private final String invalidArgTypesErrorMsg;
		private final String invalidRegexErrorMsg;
		private final Class<T0> paramClass0;
		private final Class<T1> paramClass1;
		private final Matcher<T0, T1> matcher;
		private final FirstOrderFunctionSignature<BooleanValue> funcSig;

		private CallFactory(FirstOrderFunctionSignature<BooleanValue> functionSig, Datatype<T0> paramType0, Datatype<T1> paramType1, Matcher<T0, T1> matcher)
		{

			this.invalidArgTypesErrorMsg = "Function " + functionSig.getName() + ": Invalid arg types: expected: " + paramType0 + "," + paramType1 + "; actual: ";
			this.invalidRegexErrorMsg = "Function " + functionSig.getName() + ": Invalid regular expression in arg#0";
			this.paramClass0 = paramType0.getValueClass();
			this.paramClass1 = paramType1.getValueClass();
			this.matcher = matcher;
			this.funcSig = functionSig;
		}

		protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			return new EagerMultiPrimitiveTypeEval<BooleanValue>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected final BooleanValue evaluate(Deque<AttributeValue> args) throws IndeterminateEvaluationException
				{
					final AttributeValue rawArg0 = args.poll();
					final AttributeValue rawArg1 = args.poll();

					final T0 arg0;
					final T1 arg1;
					try
					{
						arg0 = paramClass0.cast(rawArg0);
						arg1 = paramClass1.cast(rawArg1);
					} catch (ClassCastException e)
					{
						throw new IndeterminateEvaluationException(invalidArgTypesErrorMsg + rawArg0.getDataType() + ", " + rawArg1.getDataType(), StatusHelper.STATUS_PROCESSING_ERROR, e);
					}

					final boolean isMatched;
					try
					{
						isMatched = matcher.match(arg0, arg1);
					} catch (PatternSyntaxException e)
					{
						throw new IndeterminateEvaluationException(invalidRegexErrorMsg, StatusHelper.STATUS_PROCESSING_ERROR, e);
					}

					return BooleanValue.valueOf(isMatched);
				}
			};
		}

	}

	/**
	 * Match function call factory builder
	 * 
	 * @param <T0>
	 *            type of first value to be matched
	 * @param <T1>
	 *            type of second value to be matched against the first one
	 * 
	 */
	public interface CallFactoryBuilder<T0 extends AttributeValue, T1 extends AttributeValue>
	{
		/**
		 * Builds the match function call factory
		 * 
		 * @param functionSignature
		 *            match function signature
		 * @param paramType0
		 *            match function's first parameter type
		 * @param paramType1
		 *            match function's second parameter type
		 * @return match function call factory
		 */
		CallFactory<T0, T1> build(FirstOrderFunctionSignature<BooleanValue> functionSignature, Datatype<T0> paramType0, Datatype<T1> paramType1);
	}

	private final CallFactory<T0, T1> funcCallFactory;

	/**
	 * Creates a new <code>NonEqualTypeMatchFunction</code> based on a match method.
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param paramType0
	 *            first parameter type
	 * @param paramType1
	 *            second parameter type
	 * @param matcher
	 *            matching algorithm
	 * 
	 */
	public NonEqualTypeMatchFunction(String functionName, Datatype<T0> paramType0, Datatype<T1> paramType1, Matcher<T0, T1> matcher)
	{
		super(functionName, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramType0, paramType1));
		this.funcCallFactory = new CallFactory<>(this.functionSignature, paramType0, paramType1, matcher);
	}

	/**
	 * Creates a new <code>NonEqualTypeMatchFunction</code> based on a match method call factory builder.
	 * 
	 * @param functionName
	 *            the name of the standard match function, including the complete namespace
	 * @param paramType0
	 *            first parameter type
	 * @param paramType1
	 *            second parameter type
	 * @param callFactoryBuilder
	 *            match function call factory builder
	 * 
	 */
	public NonEqualTypeMatchFunction(String functionName, Datatype<T0> paramType0, Datatype<T1> paramType1, CallFactoryBuilder<T0, T1> callFactoryBuilder)
	{
		super(functionName, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramType0, paramType1));
		this.funcCallFactory = callFactoryBuilder.build(functionSignature, paramType0, paramType1);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		/*
		 * Actual argument types are expected to be different, therefore we use the supertype AttributeValue as generic parameter type for all when creating the function call
		 */
		return funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

	/**
	 * *-regexp-match function
	 * 
	 * @param <AV>
	 *            second parameter type
	 */
	public static class RegexpMatchCallFactoryBuilder<AV extends SimpleValue<String>> implements CallFactoryBuilder<StringValue, AV>
	{

		private final Matcher<StringValue, AV> regexMatcher = new Matcher<StringValue, AV>()
		{
			@Override
			public boolean match(StringValue regex, AV arg1)
			{
				return RegexpMatchFunctionHelper.match(regex, arg1);
			}
		};

		private class RegexpMatchCallFactory extends CallFactory<StringValue, AV>
		{
			private final RegexpMatchFunctionHelper regexFuncHelper;

			private RegexpMatchCallFactory(FirstOrderFunctionSignature<BooleanValue> functionSignature, Datatype<AV> secondParamType)
			{
				super(functionSignature, StandardDatatypes.STRING_FACTORY.getDatatype(), secondParamType, regexMatcher);
				regexFuncHelper = new RegexpMatchFunctionHelper(functionSignature, secondParamType);
			}

			@Override
			protected FirstOrderFunctionCall<BooleanValue> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
			{
				final FirstOrderFunctionCall<BooleanValue> compiledRegexFuncCall = regexFuncHelper.getCompiledRegexMatchCall(argExpressions, remainingArgTypes);
				/*
				 * compiledRegexFuncCall == null means no optimization using a pre-compiled regex could be done; in this case, use super.newCall() as usual, which will call match() down below,
				 * compiling the regex on-the-fly for each evaluation.
				 */
				return compiledRegexFuncCall == null ? super.getInstance(argExpressions, remainingArgTypes) : compiledRegexFuncCall;
			}
		}

		@Override
		public CallFactory<StringValue, AV> build(FirstOrderFunctionSignature<BooleanValue> functionSignature, Datatype<StringValue> paramType0, Datatype<AV> paramType1)
		{
			return new RegexpMatchCallFactory(functionSignature, paramType1);
		}

	}

}
