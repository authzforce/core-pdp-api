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

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

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
public class DatatypeConversionFunction<PARAM_T extends AttributeValue, RETURN_T extends AttributeValue> extends FirstOrderFunctions.SingleParameterTypedFirstOrderFunction<RETURN_T, PARAM_T>
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
		private final FirstOrderFunctionSignatures.SingleParameterTyped<RETURN, PARAM> funcSig;
		private final String invalidArgMsgPrefix;

		private CallFactory(FirstOrderFunctionSignatures.SingleParameterTyped<RETURN, PARAM> functionSignature, TypeConverter<RETURN, PARAM> converter)
		{
			this.funcSig = functionSignature;
			this.converter = converter;
			this.invalidArgMsgPrefix = "Function " + functionSignature.getName() + ": invalid arg: ";
		}

		public FirstOrderFunctionCall<RETURN> getInstance(List<Expression<?>> argExpressions, Datatype<?>[] remainingArgTypes)
		{
			return new EagerSinglePrimitiveTypeEval<RETURN, PARAM>(funcSig, argExpressions, remainingArgTypes)
			{
				@Override
				protected RETURN evaluate(Deque<PARAM> args) throws IndeterminateEvaluationException
				{
					final PARAM arg0 = args.getFirst();
					try
					{
						return converter.convert(arg0);
					} catch (IllegalArgumentException e)
					{
						throw new IndeterminateEvaluationException(invalidArgMsgPrefix + arg0, StatusHelper.STATUS_PROCESSING_ERROR, e);
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
	public DatatypeConversionFunction(String functionID, Datatype<PARAM_T> paramType, Datatype<RETURN_T> returnType, TypeConverter<RETURN_T, PARAM_T> converter)
	{
		super(functionID, returnType, false, Arrays.asList(paramType));
		this.funcCallFactory = new CallFactory<>(functionSignature, converter);
	}

	/** {@inheritDoc} */
	@Override
	public FirstOrderFunctionCall<RETURN_T> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
	{
		return this.funcCallFactory.getInstance(argExpressions, remainingArgTypes);
	}

}
