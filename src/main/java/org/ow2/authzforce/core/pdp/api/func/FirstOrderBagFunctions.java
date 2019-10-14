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
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerBagEval;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerPartlyBagEval;
import org.ow2.authzforce.core.pdp.api.func.BaseFirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeDatatype;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import com.google.common.collect.Sets;

/**
 * First-order bag functions, as opposed to the higher-order bag functions (see {@link HigherOrderBagFunction}); such as the Bag functions of section A.3.10, and the Set functions of A.3.11 of the
 * XACML spec.
 *
 * 
 * @version $Id: $
 */
public final class FirstOrderBagFunctions
{

	/**
	 * Generic 'type-one-and-only' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class SingletonBagToPrimitive<AV extends AttributeValue> extends SingleParameterTypedFirstOrderFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-one-and-only' functions
		 */
		public static final String NAME_SUFFIX_ONE_AND_ONLY = "-one-and-only";

		private final IndeterminateEvaluationException invalidArgEmptyException;

		/**
		 * Constructor
		 * 
		 * @param paramType
		 *            bag's primitive datatype
		 * @param paramBagType
		 *            bag datatype
		 */
		public SingletonBagToPrimitive(final Datatype<AV> paramType, final BagDatatype<AV> paramBagType)
		{
			super(paramBagType.getElementType().getFunctionIdPrefix() + NAME_SUFFIX_ONE_AND_ONLY, paramType, false, Arrays.asList(paramBagType));
			this.invalidArgEmptyException = new IndeterminateEvaluationException("Function " + this + ": Invalid arg #0: empty bag or bag size > 1. Required: one and only one value in bag.",
					XacmlStatusCode.PROCESSING_ERROR.value());
		}

		@Override
		public FirstOrderFunctionCall<AV> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<AV, AV>(functionSignature, argExpressions)
			{

				@Override
				protected final AV evaluate(final Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}
			};
		}

		private AV eval(final Bag<AV> bag) throws IndeterminateEvaluationException
		{
			if (bag.size() != 1)
			{
				throw invalidArgEmptyException;
			}

			return bag.getSingleElement();
		}
	}

	/**
	 * Generic 'type-bag-size' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class BagSize<AV extends AttributeValue> extends SingleParameterTypedFirstOrderFunction<IntegerValue, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag-size' functions
		 */
		public static final String NAME_SUFFIX_BAG_SIZE = "-bag-size";

		/**
		 * Constructor
		 * 
		 * @param paramBagType
		 *            bag datatype
		 */
		public BagSize(final BagDatatype<AV> paramBagType)
		{
			super(paramBagType.getElementType().getFunctionIdPrefix() + NAME_SUFFIX_BAG_SIZE, StandardDatatypes.INTEGER, false, Arrays.asList(paramBagType));
		}

		@Override
		public FirstOrderFunctionCall<IntegerValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<IntegerValue, AV>(functionSignature, argExpressions)
			{

				@Override
				protected final IntegerValue evaluate(final Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}

			};
		}

		private static IntegerValue eval(final Bag<?> bag)
		{
			return IntegerValue.valueOf(bag.size());
		}
	}

	/**
	 * Generic 'type-is-in' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class BagContains<AV extends AttributeValue> extends MultiParameterTypedFirstOrderFunction<BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-is-in' functions
		 */
		public static final String NAME_SUFFIX_IS_IN = "-is-in";

		private final Class<AV[]> arrayClass;

		private final BagDatatype<AV> bagType;

		/**
		 * Constructor
		 * 
		 * @param paramType
		 *            bag's primitive datatype
		 * @param paramBagType
		 *            bag datatype
		 * @param paramArrayClass
		 *            primitive value array class
		 */
		public BagContains(final Datatype<AV> paramType, final BagDatatype<AV> paramBagType, final Class<AV[]> paramArrayClass)
		{
			super(paramBagType.getElementType().getFunctionIdPrefix() + NAME_SUFFIX_IS_IN, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramType, paramBagType));
			this.arrayClass = paramArrayClass;
			this.bagType = paramBagType;
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerPartlyBagEval<BooleanValue, AV>(functionSignature, bagType, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final BooleanValue evaluate(final Deque<AV> primArgsBeforeBag, final Bag<AV>[] bagArgs, final AV[] remainingArgs) throws IndeterminateEvaluationException
				{
					return BooleanValue.valueOf(eval(primArgsBeforeBag.getFirst(), bagArgs[0]));
				}

			};
		}

		/**
		 * Tests whether a bag contains a given primitive value
		 * 
		 * @param arg0
		 *            primitive value
		 * @param bag
		 *            bag
		 * @return true iff {@code arg0} is in {@code bag}
		 */
		public static <V extends AttributeValue> boolean eval(final V arg0, final Bag<V> bag)
		{
			return bag.contains(arg0);
		}
	}

	/**
	 * Generic 'type-bag' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class PrimitiveToBag<AV extends AttributeValue> extends SingleParameterTypedFirstOrderFunction<Bag<AV>, AV>
	{
		/**
		 * Function ID suffix for 'primitiveType-bag' functions
		 */
		public static final String NAME_SUFFIX_BAG = "-bag";

		private final Datatype<AV> paramType;

		/**
		 * Constructor
		 * 
		 * @param paramType
		 *            bag's primitive datatype
		 * @param paramBagType
		 *            bag datatype
		 */
		public PrimitiveToBag(final Datatype<AV> paramType, final BagDatatype<AV> paramBagType)
		{
			super(paramBagType.getElementType().getFunctionIdPrefix() + NAME_SUFFIX_BAG, paramBagType, true, Arrays.asList(paramType));
			this.paramType = paramType;
		}

		@Override
		public FirstOrderFunctionCall<Bag<AV>> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<Bag<AV>, AV>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected Bag<AV> evaluate(final Deque<AV> args) throws IndeterminateEvaluationException
				{
					return Bags.newBag(paramType, args);
				}
			};
		}
	}

	/**
	 * 
	 * Base class of all *-set functions
	 * 
	 * @param <AV>
	 *            primitive type of elements in bag/set
	 * @param <RETURN>
	 *            return type
	 */
	public static abstract class SetFunction<AV extends AttributeValue, RETURN extends Value> extends SingleParameterTypedFirstOrderFunction<RETURN, Bag<AV>>
	{

		private static final IllegalArgumentException UNDEF_PARAM_TYPES_ARG_EXCEPTION = new IllegalArgumentException("Undefined function parameter types");

		private static <T> List<T> validate(final List<T> parameterTypes)
		{
			if (parameterTypes == null || parameterTypes.isEmpty())
			{
				throw UNDEF_PARAM_TYPES_ARG_EXCEPTION;
			}

			return parameterTypes;
		}

		/**
		 * Creates instance
		 * 
		 * @param functionIdSuffix
		 *            suffix to functionId; resulting functionId = {@code parameterType.getTypeParameter().getFuncIdPrefix()+ functionIdSuffix} function ID
		 * @param returnType
		 *            return type
		 * @param varArgs
		 *            variable-length parameter (the number of parameters to set function is variable)
		 * @param parameterTypes
		 *            parameter types
		 */
		public SetFunction(final String functionIdSuffix, final Datatype<RETURN> returnType, final boolean varArgs, final List<? extends BagDatatype<AV>> parameterTypes)
		{
			super(validate(parameterTypes).get(0).getElementType().getFunctionIdPrefix() + functionIdSuffix, returnType, varArgs, parameterTypes);
		}

		@Override
		public final FirstOrderFunctionCall<RETURN> newCall(final List<Expression<?>> argExpressions, final Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<RETURN, AV>(functionSignature, argExpressions)
			{

				@Override
				protected RETURN evaluate(final Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs);
				}
			};
		}

		abstract protected RETURN eval(Bag<AV>[] bagArgs);
	}

	/**
	 * Generic 'type-intersection' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class Intersection<AV extends AttributeValue> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-intersection' functions
		 */
		public static final String NAME_SUFFIX_INTERSECTION = "-intersection";

		private final Datatype<AV> paramType;

		/**
		 * Constructor
		 * 
		 * @param paramType
		 *            bag's primitive datatype
		 * @param paramBagType
		 *            bag datatype
		 */
		public Intersection(final Datatype<AV> paramType, final BagDatatype<AV> paramBagType)
		{
			super(NAME_SUFFIX_INTERSECTION, paramBagType, false, Arrays.asList(paramBagType, paramBagType));
			this.paramType = paramType;
		}

		@Override
		protected Bag<AV> eval(final Bag<AV>[] bagArgs)
		{
			return Bags.newBag(this.paramType, eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> Set<V> eval(final Bag<V> bag0, final Bag<V> bag1)
		{
			return Sets.intersection(bag0.elements().elementSet(), bag1.elements().elementSet());
		}

	}

	/**
	 * Generic 'type-at-least-one-member-of' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class AtLeastOneMemberOf<AV extends AttributeValue> extends SetFunction<AV, BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-at-least-one-member-of' functions
		 */
		public static final String NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF = "-at-least-one-member-of";

		/**
		 * Constructor
		 * 
		 * @param paramBagType
		 *            bag datatype
		 */
		public AtLeastOneMemberOf(final BagDatatype<AV> paramBagType)
		{
			super(NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramBagType, paramBagType));
		}

		@Override
		protected BooleanValue eval(final Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(final Bag<V> bag0, final Bag<V> bag1)
		{
			for (final V bag0Val : bag0)
			{
				if (bag1.contains(bag0Val))
				{
					return true;
				}
			}

			return false;
		}

	}

	/**
	 * Generic 'type-union' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class Union<AV extends AttributeValue> extends SetFunction<AV, Bag<AV>>
	{
		/**
		 * Function ID suffix for 'primitiveType-union' functions
		 */
		public static final String NAME_SUFFIX_UNION = "-union";

		private final Datatype<AV> paramType;

		/**
		 * Constructor
		 * 
		 * @param paramType
		 *            bag's primitive datatype
		 * @param paramBagType
		 *            bag datatype
		 */
		public Union(final Datatype<AV> paramType, final BagDatatype<AV> paramBagType)
		{
			/*
			 * Union function takes two or more parameters, i.e. two parameters of a specific bag type and a variable-length (zero-to-any) parameter of the same bag type
			 */
			super(NAME_SUFFIX_UNION, paramBagType, true, Arrays.asList(paramBagType, paramBagType, paramBagType));
			this.paramType = paramType;
		}

		@Override
		protected Bag<AV> eval(final Bag<AV>[] bags)
		{
			final Set<AV> result = HashCollections.newUpdatableSet();
			for (final Bag<AV> bag : bags)
			{
				for (final AV bagVal : bag)
				{
					result.add(bagVal);
				}
			}

			return Bags.newBag(this.paramType, result);
		}
	}

	/**
	 * Generic 'type-subset' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class Subset<AV extends AttributeValue> extends SetFunction<AV, BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-subset' functions
		 */
		public static final String NAME_SUFFIX_SUBSET = "-subset";

		/**
		 * Constructor
		 * 
		 * @param paramBagType
		 *            bag datatype
		 */
		public Subset(final BagDatatype<AV> paramBagType)
		{
			super(NAME_SUFFIX_SUBSET, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramBagType, paramBagType));
		}

		@Override
		protected BooleanValue eval(final Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(final Bag<V> bag0, final Bag<V> bag1)
		{
			return bag1.elements().containsAll(bag0.elements());
		}

	}

	/**
	 * Generic 'type-set-equals' function
	 *
	 * @param <AV>
	 *            primitive datatype
	 */
	public static class SetEquals<AV extends AttributeValue> extends SetFunction<AV, BooleanValue>
	{
		/**
		 * Function ID suffix for 'primitiveType-set-equals' functions
		 */
		public static final String NAME_SUFFIX_SET_EQUALS = "-set-equals";

		/**
		 * Constructor
		 * 
		 * @param paramBagType
		 *            bag datatype
		 */
		public SetEquals(final BagDatatype<AV> paramBagType)
		{
			super(NAME_SUFFIX_SET_EQUALS, StandardDatatypes.BOOLEAN, false, Arrays.asList(paramBagType, paramBagType));
		}

		@Override
		protected BooleanValue eval(final Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(final Bag<V> bag0, final Bag<V> bag1)
		{
			return bag0.elements().elementSet().equals(bag1.elements().elementSet());
		}

	}

	/**
	 * Creates/gets all first-order bag functions taking a given primitive datatype as bag's primitive type, i.e. the equivalents of standard
	 * <code>urn:oasis:names:tc:xacml:x.x:function:type${suffix}</code> functions, where <code>urn:oasis:names:tc:xacml:x.x:function:type</code> is replaced by a given prefix ({@code functionIdPrefix}
	 * ) and the suffix takes the following values (one per created function):
	 * <ul>
	 * <li>{@code -one-and-only}: converts a given singleton bag to a the single primitive value in the bag</li>
	 * <li>{@code -bag-size}: gives the size of a given bag</li>
	 * <li>{@code -is-in}: tests whether the bag contains a given primitive value</li>
	 * <li>{@code -bag}: creates a singleton bag from a given primitive value</li>
	 * <li>{@code -intersection}: intersection of given bags</li>
	 * <li>{@code -at-least-one-member-of}: tests whether one of the values in a given bag is in another given bag</li>
	 * <li>{@code -union}: union of bags</li>
	 * <li>{@code -subset}: tests whether all values of a given bag are in another given bag</li>
	 * <li>{@code -set-equals}: tests whether bags are equal regardless of order</li>
	 * </ul>
	 * 
	 * @param paramType
	 *            parameter datatype
	 * @return first-order bag functions taking the given primitive datatype as bag's primitive type
	 */
	public static <AV extends AttributeValue> Set<Function<?>> getFunctions(final AttributeDatatype<AV> paramType)
	{
		final BagDatatype<AV> paramBagType = paramType.getBagDatatype();
		final Class<AV[]> paramArrayClass = paramType.getArrayClass();
		return HashCollections.<Function<?>>newImmutableSet(new Function[] {
				/**
				 * 
				 * Single-bag function group, i.e. group of bag functions that takes only one bag as parameter, or no bag parameter but returns a bag. Defined in section A.3.10. As opposed to Set
				 * functions that takes multiple bags as parameters.
				 * 
				 */
				new SingletonBagToPrimitive<>(paramType, paramBagType), new BagSize<>(paramBagType), new BagContains<>(paramType, paramBagType, paramArrayClass),
				new PrimitiveToBag<>(paramType, paramBagType),
				/**
				 * 
				 * Add bag functions that takes multiple bags as parameters. Defined in section A.3.11.
				 * 
				 */
				new Intersection<>(paramType, paramBagType), new AtLeastOneMemberOf<>(paramBagType), new Union<>(paramType, paramBagType), new Subset<>(paramBagType), new SetEquals<>(paramBagType) });
	}

}
