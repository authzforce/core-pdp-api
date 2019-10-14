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
package org.ow2.authzforce.core.pdp.api.func;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * A superclass of primitive datatype conversion functions such as double-to-integer, integer-to-double, *-from-string, *-to-string, etc. May be used for non-standard datatype conversion functions as
 * well. A datatype conversion function takes one argument of a given type and converts that argument to another given type.
 *
 * @param <PARAM_T>
 *            parameter/input type
 * @param <RETURN_T>
 *            return/output type
 * 
 * @version $Id: $
 */
public class DatatypeConversionFunction<PARAM_T extends AttributeValue, RETURN_T extends AttributeValue> extends SingleParameterTypedFirstOrderFunction<RETURN_T, PARAM_T>
{

	/**
	 * Data converter
	 *
	 * @param <RETURN>
	 *            return type
	 * @param <PARAM>
	 *            parameter type
	 */
	public interface TypeConverter<RETURN, PARAM>
	{

		/**
		 * Converts a value from a type to the other
		 * 
		 * @param arg
		 *            parameter
		 * @return value converted to the new type
		 * @throws IllegalArgumentException
		 *             if {@code arg} cannot be converted to the expected return type
		 */
		RETURN convert(PARAM arg) throws IllegalArgumentException;
	}

	private static final class CallFactory<RETURN extends Value, PARAM extends AttributeValue>
	{
		private final TypeConverter<RETURN, PARAM> converter;
		private final SingleParameterTypedFirstOrderFunctionSignature<RETURN, PARAM> funcSig;
		private final String invalidArgMsgPrefix;

		private CallFactory(final SingleParameterTypedFirstOrderFunctionSignature<RETURN, PARAM> functionSignature, final TypeConverter<RETURN, PARAM> converter)
		{
			this.funcSig = functionSignature;
			this.converter = converter;
			this.invalidArgMsgPrefix = "Function " + functionSignature.getName() + ": invalid arg: ";
		}

		public FirstOrderFunctionCall<RETURN> getInstance(final List<Expression<?>> argExpressions, final Datatype<?>[] remainingArgTypes)
		{
			return new EagerSinglePrimitiveTypeEval<RETURN, PARAM>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected RETURN evaluate(final Deque<PARAM> args) throws IndeterminateEvaluationException
				{
					final PARAM arg0 = args.getFirst();
					try
					{
						return converter.convert(arg0);
					}
					catch (final IllegalArgumentException e)
					{
						throw new IndeterminateEvaluationException(invalidArgMsgPrefix + arg0, XacmlStatusCode.PROCESSING_ERROR.value(), e);
					}
				}

			};
		}
	}

	private final CallFactory<RETURN_T, PARAM_T> funcCallFactory;

	/**
	 * Creates a new <code>DatatypeConversionFunction</code> object.
	 * 
	 * @param functionID
	 *            function ID
	 * 
	 * @param paramType
	 *            parameter type
	 * @param returnType
	 *            return type
	 * @param converter
	 *            type converter
	 * 
	 */
	public DatatypeConversionFunction(final String functionID, final Datatype<PARAM_T> paramType, final Datatype<RETURN_T> returnType, final TypeConverter<RETURN_T, PARAM_T> converter)
	{
		super(functionID, returnType, false, Arrays.asList(paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, converter);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<RETURN_T> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

}
