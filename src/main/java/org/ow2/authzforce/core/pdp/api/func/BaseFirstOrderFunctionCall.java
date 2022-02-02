/*
 * Copyright 2012-2022 THALES.
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

import java.util.*;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.expression.Expressions;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

/**
 * This class provides a skeletal implementation of the {@link FirstOrderFunctionCall} interface, to minimize the effort required to implement this interface. It requires a function definition and
 * given arguments to be passed to the function, and based on this information, enforces type checking. It is the recommended way of calling any {@link FirstOrderFunction} instance.
 * <p>
 * Some of the arguments (expressions) may not be known in advance, but only at evaluation time (when calling {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}). For example, when using
 * a FirstOrderFunction as a sub-function of the Higher-Order function 'any-of', the last arguments of the sub-function are determined during evaluation, after evaluating the expression of the last
 * input in the context, and getting the various values in the result bag.
 * <p>
 * In the case of such evaluation-time args, you must pass their types (the datatype of the last input bag in the previous example) as the <code>remainingArgTypes</code> parameters to the
 * {@link BaseFirstOrderFunctionCall} subclass (e.g. {@link EagerEval} implementation) constructor, and correspond to the types of the <code>remainingArgs</code> passed later as parameters to
 * {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}.
 * 
 * @param <RETURN>
 *            function return type
 * 
 * 
 */
public abstract class BaseFirstOrderFunctionCall<RETURN extends Value> implements FirstOrderFunctionCall<RETURN>
{
	private static final IllegalArgumentException EVAL_ARGS_NULL_INPUT_STACK_EXCEPTION = new IllegalArgumentException("Input stack to store evaluation results is NULL");

	/**
	 * Evaluates primitive argument expressions in the given context, and stores all result values in a given linear collection of a specific datatype.
	 * 
	 * @param args
	 *            (mandatory) function arguments
	 * @param context
	 *           Individual Decision evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used
	 * @param argReturnType
	 *            return type of argument expression evaluation
	 * @param resultsToUpdate
	 *            attribute values to be updated with results from evaluating all <code>args</code> in <code>context</code>; the specified type <code>AV</code> of array elements must be a supertype of
	 *            any expected arg evaluation result datatype. Used as the method result if not null; If null, a new instance is created.
	 * @return results containing all evaluation results.
	 * @throws IndeterminateEvaluationException
	 *             if evaluation of one of the arg failed, or <code>T</code> is not a supertype of the result value datatype
	 * @throws IllegalArgumentException
	 *             if <code>resultsToUpdate != null && resultsToUpdate < args.size()</code>
	 */
	private static <AV extends AttributeValue> Deque<AV> evalPrimitiveArgs(final List<? extends Expression<?>> args, final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final Datatype<AV> argReturnType,
																		   final Deque<AV> resultsToUpdate) throws IndeterminateEvaluationException
	{
		assert args != null;
		final Deque<AV> results = resultsToUpdate == null ? new ArrayDeque<>() : resultsToUpdate;

		for (final Expression<?> arg : args)
		{
			// get and evaluate the next parameter
			/*
			 * The types of arguments have already been checked with checkInputs(), so casting to returnType should work.
			 */
			final AV argVal;
			try
			{
				argVal = Expressions.eval(arg, context, mdpContext, argReturnType);
			} catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Indeterminate arg #" + results.size(), e.getStatusCode(), e);
			}

			results.add(argVal);
		}

		return results;
	}

	/**
	 * Evaluates primitive argument expressions in the given context, and stores all result values in a given linear collection.
	 * 
	 * @param args
	 *            (mandatory) function arguments
	 * @param context
	 *            evaluation context
	 * @param mdpContext
	 * 	 the context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used.
	 * @param resultsToUpdate
	 *            attribute values to be updated with results from evaluating all <code>args</code> in <code>context</code>. Used as the method result if not null; If null, a new instance is created.
	 * @throws IndeterminateEvaluationException
	 *             if evaluation of one of the arg failed
	 * @throws IllegalArgumentException
	 *             if <code>resultsToUpdate != null && resultsToUpdate < args.size()</code>
	 */
	private static void evalPrimitiveArgs(final List<? extends Expression<?>> args, final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final Deque<AttributeValue> resultsToUpdate)
	        throws IndeterminateEvaluationException
	{
		assert args != null;
		final Deque<AttributeValue> results = resultsToUpdate == null ? new ArrayDeque<>() : resultsToUpdate;

		for (final Expression<?> arg : args)
		{
			// get and evaluate the next parameter
			/*
			 * The types of arguments have already been checked with checkInputs(), so casting to returnType should work.
			 */
			final AttributeValue argVal;
			try
			{
				argVal = Expressions.evalPrimitive(arg, context, mdpContext);
			} catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Indeterminate arg #" + results.size(), e.getStatusCode(), e);
			}

			results.add(argVal);
		}
	}

	private static <AV extends AttributeValue> Bag<AV>[] evalBagArgs(final List<Expression<?>> args, final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final Datatype<Bag<AV>> argReturnType,
	        final Bag<AV>[] results) throws IndeterminateEvaluationException
	{
		assert args != null;

		if (results == null)
		{
			throw EVAL_ARGS_NULL_INPUT_STACK_EXCEPTION;
		}

		if (results.length < args.size())
		{
			throw new IllegalArgumentException(
			        "Invalid size of input array to store Expression evaluation results: " + results.length + ". Required (>= number of input Expressions): >= " + args.size());
		}

		int resultIndex = 0;
		for (final Expression<?> arg : args)
		{
			// get and evaluate the next parameter
			/*
			 * The types of arguments have already been checked with checkInputs(), so casting to returnType should work.
			 */
			final Bag<AV> argResult;
			try
			{
				argResult = Expressions.eval(arg, context, mdpContext, argReturnType);
			} catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException("Indeterminate arg #" + resultIndex, e.getStatusCode(), e);
			}

			results[resultIndex] = argResult;
			resultIndex++;
		}

		return results;
	}

	private static void checkArgType(final Datatype<?> argType, final int argIndex, final Datatype<?> expectedType, final String funcId) throws IllegalArgumentException
	{
		if (!argType.equals(expectedType))
		{
			throw new IllegalArgumentException("Function " + funcId + ": type of arg #" + argIndex + " not valid: " + argType + ". Required: " + expectedType + ".");
		}
	}

	private static void checkArgType(final AttributeValue arg, final int argIndex, final Datatype<?> expectedType, final String funcId) throws IllegalArgumentException
	{
		if(expectedType == null) {
			throw new IllegalArgumentException("Function " + funcId + ": arg #" + argIndex + " is unexpected.");
		}

		if (!expectedType.isInstance(arg))
		{
			throw new IllegalArgumentException("Function " + funcId + ": type of arg #" + argIndex + " does not match required type: " + expectedType + ".");
		}
	}

	private interface RequestTimeArgCountChecker
	{
		void check(int requestTimeArgCount) throws IndeterminateEvaluationException;
	}

	private static final RequestTimeArgCountChecker NULL_REQUEST_TIME_ARG_COUNT_CHECKER = requestTimeArgCount -> {
		// null checker does nothing, just there for polymorphism
	};

	private static final class DefaultRequestTimeArgCountChecker implements RequestTimeArgCountChecker
	{
		private final int minRequestTimeArgCount;
		private final String funcDesc;

		private DefaultRequestTimeArgCountChecker(final String functionDescription, final int minRequestTimeArgCount)
		{
			this.minRequestTimeArgCount = minRequestTimeArgCount;
			this.funcDesc = functionDescription;
		}

		@Override
		public void check(final int requestTimeArgCount) throws IndeterminateEvaluationException
		{
			if (requestTimeArgCount < minRequestTimeArgCount)
			{
				throw new IndeterminateEvaluationException(
				        "Invalid number of request-time args (" + requestTimeArgCount + ") passed to function" + funcDesc + ". Required: >= " + minRequestTimeArgCount,
				        XacmlStatusCode.PROCESSING_ERROR.value());
			}
		}
	}

	private final String funcId;
	private final List<? extends Datatype<?>> expectedParamTypesForRemainingArgs;
	private final RequestTimeArgCountChecker requestTimeArgCountChecker;
	private final Datatype<RETURN> returnType;

	/**
	 * Instantiates a function call, including the validation of arguments ({@code inputExpressions} ) according to the function definition.
	 * 
	 * @param functionSig
	 *            signature of function to which this call applies
	 * @param argExpressions
	 *            function arguments (expressions)
	 * 
	 * @param remainingArgTypes
	 *            types of arguments of which the actual Expressions are unknown at this point, but will be known and passed at evaluation time as <code>remainingArgs</code> parameter to
	 *            {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}, then {@link #evaluate(EvaluationContext, Optional, AttributeValue...)}. Only non-bag/primitive values are valid
	 *            <code>remainingArgs</code> to prevent varargs warning in {@link #evaluate(EvaluationContext, Optional, AttributeValue...)} (potential heap pollution via varargs parameter) that would be caused
	 *            by using a parameterized type such as Value/Collection to represent both bags and primitives.
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function or one of <code>remainingArgTypes</code> is a bag type.
	 */
	public BaseFirstOrderFunctionCall(final FirstOrderFunctionSignature<RETURN> functionSig, final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes)
	        throws IllegalArgumentException
	{
		this.funcId = functionSig.name;
		final List<? extends Datatype<?>> paramTypes = functionSig.getParameterTypes();
		final int arity = paramTypes.size();
		if (arity < 1)
		{
			throw new IllegalArgumentException("Invalid function: " + funcId + ": does not have any parameter. Required arity: >= 1.");
		}

		// check number of arguments
		final int initialArgCount = argExpressions.size();
		final int totalActualArgCount = initialArgCount + remainingArgTypes.length;
		if (functionSig.isVarArgs())
		{
			/*
			 * Last parameter is variable-length (varargs) -> it may occur 0 or more times in function call arguments, so min number of arguments is (arity - 1). We already checked that arity >= 1.
			 */
			final int minArgCount = arity - 1;
			if (totalActualArgCount < minArgCount)
			{
				throw new IllegalArgumentException("Invalid number of args (" + totalActualArgCount + ") passed to varargs function: " + funcId + ". Required: >= " + minArgCount);
			}

			// We will validate remainingArgs only, so we skip all the initial arguments, and therefore check the list
			// starting at index = initialArgCount
			// if initialArgCount < arity, or arity - 1 = minArgCount if initialArgCount >= arity. In tha latter case,
			// it means all remainingArgs are
			// repetitions of the vararg (last parameter), therefore of the same type which is the
			// last parameter type (index = arity - 1 = minArgCount)
			this.expectedParamTypesForRemainingArgs = paramTypes.subList(initialArgCount < arity ? initialArgCount : minArgCount, arity);
			// if(initialArgCount >= minArgCount), there is already enough args, so we don't care how many
			// request-time/remaining args there will be -> use
			// null-checker that does nothing
			this.requestTimeArgCountChecker = initialArgCount >= minArgCount ? NULL_REQUEST_TIME_ARG_COUNT_CHECKER : new DefaultRequestTimeArgCountChecker(funcId, minArgCount - initialArgCount);

		} else
		{
			if (totalActualArgCount != arity)
			{
				throw new IllegalArgumentException("Invalid number of args (" + totalActualArgCount + ") to function: " + funcId + ". Required: " + arity);
			}

			// We will validate remainingArgs only, so we skip all the initial arguments, and therefore check the list
			// starting at index = initialArgCount
			this.expectedParamTypesForRemainingArgs = paramTypes.subList(initialArgCount, arity);
			this.requestTimeArgCountChecker = new DefaultRequestTimeArgCountChecker(funcId, arity - initialArgCount);
		}

		// check types of arguments
		final Iterator<? extends Datatype<?>> expectedTypesIterator = paramTypes.iterator();
		Datatype<?> expectedType = null;
		int paramIndex = 0;
		for (final Expression<?> argExpression : argExpressions)
		{
			if (expectedTypesIterator.hasNext())
			{
				// function arity > 0 as a precondition to this method (see above), so we get there at least once and
				// expectedType gets initialized
				expectedType = expectedTypesIterator.next();
			}

			checkArgType(argExpression.getReturnType(), paramIndex, expectedType, funcId);
			paramIndex++;
		}

		for (final Datatype<?> remainingArgType : remainingArgTypes)
		{
			if (remainingArgType.getTypeParameter().isPresent())
			{
				throw new IllegalArgumentException("Invalid type (" + remainingArgType + ") of request-time arg for parameter #" + paramIndex + " of function: " + funcId
				        + ". Only primitive type are allowed for request-time args.");
			}

			if (expectedTypesIterator.hasNext())
			{
				expectedType = expectedTypesIterator.next();
			}

			checkArgType(remainingArgType, paramIndex, expectedType, funcId);
			paramIndex++;
		}

		this.returnType = functionSig.getReturnType();
	}

	@Override
	public final RETURN evaluate(final EvaluationContext context, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
	{
		return evaluate(context, mdpContext, (AttributeValue[]) null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall#evaluate(org.ow2.authzforce.core.pdp.api.EvaluationContext, boolean, org.ow2.authzforce.core.pdp.api.value.AttributeValue)
	 */
	@Override
	public final RETURN evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final boolean checkRemainingArgTypes, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
	{
		if (checkRemainingArgTypes)
		{
			// check number of arguments
			this.requestTimeArgCountChecker.check(remainingArgs.length);

			// check types of remaining arguments
			final Iterator<? extends Datatype<?>> expectedTypesIterator = expectedParamTypesForRemainingArgs.iterator();
			Datatype<?> expectedType = null;
			int paramIndex = 0;
			for (final AttributeValue remainingArg : remainingArgs)
			{
				if (expectedTypesIterator.hasNext())
				{
					expectedType = expectedTypesIterator.next();
				}

				checkArgType(remainingArg, paramIndex, expectedType, funcId);
				paramIndex++;
			}

		}

		return evaluate(context, mdpContext, remainingArgs);
	}

	@Override
	public final Datatype<RETURN> getReturnType()
	{
		return returnType;
	}

	/**
	 * Function call, for {@link FirstOrderFunction}s requiring <i>eager</i> (aka <i>greedy</i>) evaluation of ALL their arguments' expressions to actual values, before the function can be evaluated.
	 * This is the case of most functions in XACML. Exceptions (functions not using eager evaluation) are logical functions for instance, such as 'or', 'and', 'n-of'. Indeed, these functions can
	 * return the final result before all arguments have been evaluated, e.g. the 'or' function returns True as soon as one of the arguments return True, regardless of the remaining arguments.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 */
	public static abstract class EagerEval<RETURN_T extends Value> extends BaseFirstOrderFunctionCall<RETURN_T>
	{
		protected final List<Expression<?>> argExpressions;
		protected final String indeterminateArgMessage;
		protected final int totalArgCount;
		protected final int numOfSameTypePrimitiveParamsBeforeBag;
		protected final String functionId;

		/**
		 * Instantiates Function FirstOrderFunctionCall
		 * 
		 * @param functionSignature
		 *            function signature
		 * 
		 * @param args
		 *            arguments' Expressions
		 * @param remainingArgTypes
		 *            types of arguments following <code>args</code>, and of which the actual Expression is unknown at this point, but will be known and passed at evaluation time as
		 *            <code>remainingArgs</code> parameter to {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}, then {@link #evaluate(EvaluationContext, Optional, AttributeValue...)}.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected EagerEval(final FirstOrderFunctionSignature<RETURN_T> functionSignature, final List<Expression<?>> args, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSignature, args, remainingArgTypes);
			final List<? extends Datatype<?>> paramTypes = functionSignature.getParameterTypes();
			final String funcId = functionSignature.getName();

			/*
			 * Determine compatible eager-eval function call if any, depending on number of primitive parameters against total number of parameters. (We do not check here whether all parameters have
			 * same primitive datatype in the function signature, as you can always use the EagerSinglePrimitiveTypeEval with supertype AttributeValue.)
			 */
			int primParamCount = 0;
			Datatype<?> commonPrimitiveType = null;
			for (final Datatype<?> paramType : paramTypes)
			{
				if (paramType.getTypeParameter().isEmpty())
				{
					// primitive type
					if (primParamCount == 0)
					{
						commonPrimitiveType = paramType;
					} else
					{
						// not the first primitive parameter
						if (!paramType.equals(commonPrimitiveType))
						{
							// not the same type
							commonPrimitiveType = null;
						}
					}

					primParamCount++;
				}
			}

			// parameters have same primitive datatype
			if (primParamCount == paramTypes.size())
			{
				// All parameters are primitive
				if (commonPrimitiveType == null)
				{
					// multiple/different types -> use EagerMultiPrimitiveTypeEval.class
					if (!EagerMultiPrimitiveTypeEval.class.isAssignableFrom(this.getClass()))
					{
						throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerMultiPrimitiveTypeEval.class
						        + " or any subclass instead, when all parameters are primitive but not of the same datatypes.");
					}
				} else
				{
					// same common type -> use EagerSinglePrimitiveTypeEval.class
					if (!EagerSinglePrimitiveTypeEval.class.isAssignableFrom(this.getClass()))
					{
						throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerSinglePrimitiveTypeEval.class
						        + " or any subclass instead when all parameters are primitive and with same datatype.");
					}
				}
			} else if (primParamCount == 0)
			{
				// no primitive parameters -> all parameters are bag -> use EagerBagEval.class
				if (!EagerBagEval.class.isAssignableFrom(this.getClass()))
				{
					throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerBagEval.class
					        + " or any subclass instead when all parameters are bag.");
				}
			} else
			{
				// partly primitive, partly bag -> use EagerPartlyBagEval
				/*
				 * For anonymous class used often to instantiate function call, call Class#getSuperClass() to get actual FunctionCall class implemented.
				 */
				if (!EagerPartlyBagEval.class.isAssignableFrom(this.getClass()))
				{
					throw new IllegalArgumentException("Invalid type of function call used for function '" + funcId + "': " + this.getClass() + ". Use " + EagerPartlyBagEval.class
					        + " or any subclass instead when there are both primitive and bag parameters.");
				}
			}
			// END OF determining type of eager-eval function call
			this.numOfSameTypePrimitiveParamsBeforeBag = primParamCount;
			this.argExpressions = args;
			this.functionId = funcId;
			this.indeterminateArgMessage = "Function " + funcId + ": indeterminate arg";
			// total number of arguments to the function
			this.totalArgCount = args.size() + remainingArgTypes.length;
		}
	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL their arguments' expressions to actual values, before the function can be evaluated. All arguments
	 * must be primitive values but may not have the same primitive datatype.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 */
	public static abstract class EagerMultiPrimitiveTypeEval<RETURN_T extends Value> extends EagerEval<RETURN_T>
	{
		/**
		 * Instantiates Function call
		 * 
		 * @param functionSig
		 *            function signature
		 * 
		 * @param args
		 *            arguments' Expressions
		 * @param remainingArgTypes
		 *            types of arguments following <code>args</code>, and of which the actual Expression is unknown at this point, but will be known and passed at evaluation time as
		 *            <code>remainingArgs</code> parameter to {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}, then {@link #evaluate(EvaluationContext, Optional, AttributeValue...)}.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected EagerMultiPrimitiveTypeEval(final FirstOrderFunctionSignature<RETURN_T> functionSig, final List<Expression<?>> args, final Datatype<?>... remainingArgTypes)
		        throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param args
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract RETURN_T evaluate(Deque<AttributeValue> args) throws IndeterminateEvaluationException;

		@Override
		public final RETURN_T evaluate(final EvaluationContext context, Optional<EvaluationContext> mdpContext, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			final Deque<AttributeValue> finalArgs = new ArrayDeque<>(totalArgCount);
			if (argExpressions != null)
			{
				try
				{
					evalPrimitiveArgs(argExpressions, context, mdpContext, finalArgs);
				} catch (final IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this.indeterminateArgMessage, e.getStatusCode(), e);
				}
			}

			if (remainingArgs != null)
			{
				/*
				 * remainingArgs (following the initial args, therefore starting at index = initialArgCount)
				 */
				for (final AttributeValue remainingArg : remainingArgs)
				{
					finalArgs.add(remainingArg);
				}
			}

			return evaluate(finalArgs);
		}
	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL their arguments' expressions to actual values, before the function can be evaluated. All arguments
	 * must be primitive values and have the same primitive datatype.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PARAM_T>
	 *            arg values' common (super)type. If argument expressions return different data-types, the common concrete supertype of all may be specified.
	 * 
	 * 
	 */
	public static abstract class EagerSinglePrimitiveTypeEval<RETURN_T extends Value, PARAM_T extends AttributeValue> extends EagerEval<RETURN_T>
	{
		private final Datatype<PARAM_T> parameterType;

		/**
		 * Instantiates Function call
		 * 
		 * @param functionSig
		 *            function signature
		 * 
		 * @param args
		 *            arguments' Expressions
		 * @param remainingArgTypes
		 *            types of arguments following <code>args</code>, and of which the actual Expression is unknown at this point, but will be known and passed at evaluation time as
		 *            <code>remainingArgs</code> parameter to {@link #evaluate(EvaluationContext, Optional, boolean, AttributeValue...)}, then {@link #evaluate(EvaluationContext, Optional, AttributeValue...)}.
		 * @throws IllegalArgumentException
		 *             if one of <code>remainingArgTypes</code> is a bag type.
		 */
		protected EagerSinglePrimitiveTypeEval(final SingleParameterTypedFirstOrderFunctionSignature<RETURN_T, PARAM_T> functionSig, final List<Expression<?>> args,
		        final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);

			this.parameterType = functionSig.getParameterType();
		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param argStack
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract RETURN_T evaluate(Deque<PARAM_T> argStack) throws IndeterminateEvaluationException;

		@Override
		public final RETURN_T evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			final Deque<PARAM_T> finalArgs = new ArrayDeque<>(totalArgCount);
			if (argExpressions != null)
			{
				try
				{
					evalPrimitiveArgs(argExpressions, context, mdpContext, parameterType, finalArgs);
				} catch (final IndeterminateEvaluationException e)
				{
					throw new IndeterminateEvaluationException(this.indeterminateArgMessage, e.getStatusCode(), e);
				}
			}

			if (remainingArgs != null)
			{
				/*
				 * remainingArgs (following the initial args, therefore starting at index = initialArgCount)
				 */
				for (final AttributeValue remainingArg : remainingArgs)
				{
					try
					{
						finalArgs.add(parameterType.cast(remainingArg));
					} catch (final ClassCastException e)
					{
						throw new IndeterminateEvaluationException("Function " + this.functionId + ": Type of arg #" + finalArgs.size() + " not valid. Expected: " + parameterType + ".",
						        XacmlStatusCode.PROCESSING_ERROR.value());
					}
				}
			}

			return evaluate(finalArgs);
		}
	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL their arguments' expressions to actual values, before the function can be evaluated. All arguments
	 * must be bags, therefore no support for primitive values resolved at evaluation time (i.e. remaining args / evaluation-time args are not supported). If some ending parameters are primitive, use
	 * {@link BaseFirstOrderFunctionCall.EagerPartlyBagEval} instead.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PARAM_BAG_ELEMENT_T>
	 *            supertype of primitive elements in the parameter bag(s). If these parameter bags have elements of different primitive datatypes, the supertype of all - {@link AttributeValue} - may
	 *            be specified.
	 * 
	 * 
	 */
	public static abstract class EagerBagEval<RETURN_T extends Value, PARAM_BAG_ELEMENT_T extends AttributeValue> extends EagerEval<RETURN_T>
	{
		private final Datatype<Bag<PARAM_BAG_ELEMENT_T>> paramBagType;

		/**
		 * Instantiates Function call
		 * 
		 * @param functionSig
		 *            function signature
		 * 
		 * @param args
		 *            arguments' Expressions
		 */
		protected EagerBagEval(final SingleParameterTypedFirstOrderFunctionSignature<RETURN_T, Bag<PARAM_BAG_ELEMENT_T>> functionSig, final List<Expression<?>> args) throws IllegalArgumentException
		{
			super(functionSig, args);
			if (argExpressions == null)
			{
				/*
				 * All arguments are primitive, since there is no argExpression, and remainingArgs are always primitive
				 */
				throw new IllegalArgumentException(
				        "Function " + functionSig.getName() + ": no bag expression in arguments. At least one bag expression is required to use this type of FunctionCall: " + this.getClass());
			}

			this.paramBagType = functionSig.getParameterType();

		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @param bagArgs
		 *            function arguments
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract RETURN_T evaluate(Bag<PARAM_BAG_ELEMENT_T>[] bagArgs) throws IndeterminateEvaluationException;

		@Override
		public RETURN_T evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{

			/*
			 * No support for remainingArgs which would be primitive values, whereas all arguments for EagerBagEval are supposed to be bags. Else use EagerPartlyBagEval.
			 */
			assert remainingArgs == null;

			/*
			 * We checked in constructor that argExpressions != null
			 */
			final Bag<PARAM_BAG_ELEMENT_T>[] bagArgs;
			try
			{
				bagArgs = evalBagArgs(argExpressions, context, mdpContext, paramBagType, paramBagType.newArray(argExpressions.size()));
			} catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this.indeterminateArgMessage, e.getStatusCode(), e);
			}

			return evaluate(bagArgs);
		}

	}

	/**
	 * Function call, for functions requiring <i>eager</i> (a.k.a. <i>greedy</i>) evaluation of ALL their arguments' expressions to actual values, before the function can be evaluated. To be used only
	 * if there is a mix of primitive and bag arguments.
	 * 
	 * @param <RETURN_T>
	 *            function return type
	 * 
	 * @param <PRIMITIVE_PARAM_T>
	 *            primitive values' supertype, i.e. bag element type for bag parameter and the parameter datatype for primitive parameters. If argument expressions return different datatypes, the
	 *            supertype of all - {@link AttributeValue} - may be specified.
	 * 
	 * 
	 */
	public static abstract class EagerPartlyBagEval<RETURN_T extends Value, PRIMITIVE_PARAM_T extends AttributeValue> extends EagerEval<RETURN_T>
	{
		private final int numOfArgExpressions;
		private final BagDatatype<PRIMITIVE_PARAM_T> bagParamType;
		private final Datatype<PRIMITIVE_PARAM_T> primitiveParamType;
		private final Class<PRIMITIVE_PARAM_T[]> primitiveParamArrayClass;

		protected EagerPartlyBagEval(final FirstOrderFunctionSignature<RETURN_T> functionSig, final BagDatatype<PRIMITIVE_PARAM_T> bagParamType, final Class<PRIMITIVE_PARAM_T[]> primitiveArrayClass,
		        final List<Expression<?>> args, final Datatype<?>[] remainingArgTypes) throws IllegalArgumentException
		{
			super(functionSig, args, remainingArgTypes);
			if (argExpressions == null || (numOfArgExpressions = argExpressions.size()) <= numOfSameTypePrimitiveParamsBeforeBag)
			{
				// all arg expressions are primitive
				throw new IllegalArgumentException(
				        "Function " + functionId + ": no bag expression in arguments. At least one bag expression is required to use this type of FunctionCall: " + this.getClass());
			}

			this.bagParamType = bagParamType;
			this.primitiveParamType = bagParamType.getElementType();
			this.primitiveParamArrayClass = primitiveArrayClass;
		}

		/**
		 * Make the call with attribute values as arguments. (The pre-evaluation of argument expressions in the evaluation context is already handled internally by this class.)
		 * 
		 * @return result of the call
		 * @throws IndeterminateEvaluationException
		 *             if any error evaluating the function
		 */
		protected abstract RETURN_T evaluate(Deque<PRIMITIVE_PARAM_T> primArgsBeforeBag, Bag<PRIMITIVE_PARAM_T>[] bagArgs, PRIMITIVE_PARAM_T[] remainingArgs) throws IndeterminateEvaluationException;

		@Override
		public final RETURN_T evaluate(final EvaluationContext context, final Optional<EvaluationContext> mdpContext, final AttributeValue... remainingArgs) throws IndeterminateEvaluationException
		{
			/*
			 * We checked in constructor that argExpressions.size > numOfSameTypePrimitiveParamsBeforeBag
			 */
			final Deque<PRIMITIVE_PARAM_T> primArgsBeforeBag;
			final Bag<PRIMITIVE_PARAM_T>[] bagArgs;
			try
			{
				primArgsBeforeBag = evalPrimitiveArgs(argExpressions.subList(0, numOfSameTypePrimitiveParamsBeforeBag), context, mdpContext, primitiveParamType, null);
				final List<Expression<?>> bagArgExpressions = argExpressions.subList(numOfSameTypePrimitiveParamsBeforeBag, numOfArgExpressions);
				bagArgs = evalBagArgs(bagArgExpressions, context, mdpContext, bagParamType, bagParamType.newArray(bagArgExpressions.size()));
			} catch (final IndeterminateEvaluationException e)
			{
				throw new IndeterminateEvaluationException(this.indeterminateArgMessage, e.getStatusCode(), e);
			}

			final PRIMITIVE_PARAM_T[] castRemainingArgs;
			if (remainingArgs == null || remainingArgs.length == 0)
			{
				castRemainingArgs = null;
			} else
			{
				try
				{
					castRemainingArgs = primitiveParamArrayClass.cast(remainingArgs);
				} catch (final ClassCastException e)
				{
					throw new IndeterminateEvaluationException("Function " + functionId + ": Type of request-time args (# >= " + argExpressions.size() + ") not valid: "
					        + remainingArgs.getClass().getComponentType() + ". Required: " + primitiveParamType + ".", XacmlStatusCode.PROCESSING_ERROR.value());
				}
			}

			return evaluate(primArgsBeforeBag, bagArgs, castRemainingArgs);
		}

	}

}
