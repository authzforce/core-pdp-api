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

import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.expression.BaseVariableReference;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.VariableReference;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Higher-order bag function
 *
 * @param <RETURN_T>
 *            return type
 * @param <SUB_RETURN_PRIMITIVE_T>
 *            sub-function's return (primitive) type. Only functions returning primitive type of result are compatible with higher-order functions here.
 * 
 * @version $Id: $
 */
public abstract class HigherOrderBagFunction<RETURN_T extends Value, SUB_RETURN_PRIMITIVE_T extends AttributeValue> extends BaseFunction<RETURN_T>
{

	private final Datatype<RETURN_T> returnType;

	private final Datatype<?> subFuncReturnType;

	/**
	 * Instantiates higher-order bag function
	 * 
	 * @param functionId
	 *            function ID
	 * @param returnType
	 *            function's return type
	 * @param subFunctionReturnType
	 *            sub-function's return datatype; may be null to indicate any datatype (e.g. map function's sub-function return datatype can be any primitive type)
	 */
	protected HigherOrderBagFunction(final String functionId, final Datatype<RETURN_T> returnType, final Datatype<?> subFunctionReturnType)
	{
		super(functionId);
		this.returnType = returnType;
		this.subFuncReturnType = subFunctionReturnType;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns the type of attribute value that will be returned by this function.
	 */
	@Override
	public Datatype<RETURN_T> getReturnType()
	{
		return returnType;
	}

	/**
	 * Creates function call from sub-function definition and all inputs to higher-order function. To be overriden by OneBagOnlyFunctions (any-of/all-of)
	 *
	 * @param subFunc
	 *            first-order sub-function
	 * @param inputsAfterSubFunc
	 *            sub-function arguments
	 * @return function call
	 */
	protected abstract FunctionCall<RETURN_T> createFunctionCallFromSubFunction(FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc, List<Expression<?>> inputsAfterSubFunc);

	/** {@inheritDoc} */
	@Override
	public final FunctionCall<RETURN_T> newCall(final List<Expression<?>> inputs) throws IllegalArgumentException
	{
		final int numInputs = inputs.size();
		checkNumberOfArgs(numInputs);

		final Iterator<? extends Expression<?>> inputsIterator = inputs.iterator();
		final Expression<?> input0 = inputsIterator.next();
		// first arg must be a boolean function
		final Function<?> inputFunc;
		if (input0 instanceof Function)
		{
			inputFunc = (Function<?>) input0;
		} else if (input0 instanceof BaseVariableReference)
		{
			final Expression<?> varRefExp = ((VariableReference<?>) input0).getReferencedExpression();
			if (!(varRefExp instanceof Function))
			{
				throw new IllegalArgumentException(this + ": Invalid type of first argument: " + varRefExp.getClass().getSimpleName() + ". Required: Function");
			}

			inputFunc = (Function<?>) varRefExp;
		} else
		{
			throw new IllegalArgumentException(this + ": Invalid type of first argument: " + input0.getClass().getSimpleName() + ". Required: Function");
		}

		/*
		 * Check whether it is a FirstOrderFunction because it is the only type of function for which we have a generic way to validate argument types as done later below
		 */
		if (!(inputFunc instanceof FirstOrderFunction))
		{
			throw new IllegalArgumentException(this + ": Invalid function in first argument: " + inputFunc + " is not supported as such argument");
		}

		final Datatype<?> inputFuncReturnType = inputFunc.getReturnType();
		if (subFuncReturnType == null)
		{
			/*
			 * sub-function's return type can be any primitive datatype; check at least it is primitive
			 */
			if (inputFuncReturnType.getTypeParameter() != null)
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFuncReturnType + " (bag type). Required: any primitive type");
			}
		} else
		{
			if (!inputFuncReturnType.equals(subFuncReturnType))
			{
				throw new IllegalArgumentException(this + ": Invalid return type of function in first argument: " + inputFuncReturnType + ". Required: " + subFuncReturnType);
			}
		}

		// so now we know we have a boolean FirstOrderFunction
		@SuppressWarnings("unchecked")
		final FirstOrderFunction<SUB_RETURN_PRIMITIVE_T> subFunc = (FirstOrderFunction<SUB_RETURN_PRIMITIVE_T>) inputFunc;

		return createFunctionCallFromSubFunction(subFunc, inputs.subList(1, numInputs));
	}

	/**
	 * <p>
	 * checkNumberOfArgs
	 * </p>
	 *
	 * @param numInputs
	 *            a int.
	 */
	protected abstract void checkNumberOfArgs(int numInputs);
}
