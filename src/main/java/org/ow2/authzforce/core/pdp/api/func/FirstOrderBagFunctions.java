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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerBagEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerPartlyBagEval;
import org.ow2.authzforce.core.pdp.api.func.FirstOrderFunctionCall.EagerSinglePrimitiveTypeEval;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.BagDatatype;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.BooleanValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.IntegerValue;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * First-order bag functions, as opposed to the higher-order bag functions (see {@link HigherOrderBagFunction}); such as the Bag functions of section A.3.10,
 * and the Set functions of A.3.11 of the XACML spec.
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
		public SingletonBagToPrimitive(Datatype<AV> paramType, Datatype<Bag<AV>> paramBagType)
		{
			super(paramBagType.getTypeParameter().getFuncIdPrefix() + NAME_SUFFIX_ONE_AND_ONLY, paramType, false, Arrays.asList(paramBagType));
			this.invalidArgEmptyException = new IndeterminateEvaluationException("Function " + this + ": Invalid arg #0: empty bag or bag size > 1. Required: one and only one value in bag.", StatusHelper.STATUS_PROCESSING_ERROR);
		}

		@Override
		public FirstOrderFunctionCall<AV> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<AV, AV>(functionSignature, argExpressions)
			{

				@Override
				protected final AV evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}
			};
		}

		private AV eval(Bag<AV> bag) throws IndeterminateEvaluationException
		{
			if (bag.size() != 1)
			{
				throw invalidArgEmptyException;
			}

			return bag.getSingleValue();
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
		public BagSize(Datatype<Bag<AV>> paramBagType)
		{
			super(paramBagType.getTypeParameter().getFuncIdPrefix() + NAME_SUFFIX_BAG_SIZE, StandardDatatypes.INTEGER_FACTORY.getDatatype(), false, Arrays.asList(paramBagType));
		}

		@Override
		public FirstOrderFunctionCall<IntegerValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<IntegerValue, AV>(functionSignature, argExpressions)
			{

				@Override
				protected final IntegerValue evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
				{
					return eval(bagArgs[0]);
				}

			};
		}

		private static IntegerValue eval(Bag<?> bag)
		{
			return new IntegerValue(bag.size());
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
		public BagContains(Datatype<AV> paramType, BagDatatype<AV> paramBagType, Class<AV[]> paramArrayClass)
		{
			super(paramBagType.getTypeParameter().getFuncIdPrefix() + NAME_SUFFIX_IS_IN, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramType, paramBagType));
			this.arrayClass = paramArrayClass;
			this.bagType = paramBagType;
		}

		@Override
		public FirstOrderFunctionCall<BooleanValue> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerPartlyBagEval<BooleanValue, AV>(functionSignature, bagType, arrayClass, argExpressions, remainingArgTypes)
			{

				@Override
				protected final BooleanValue evaluate(Deque<AV> primArgsBeforeBag, Bag<AV>[] bagArgs, AV[] remainingArgs) throws IndeterminateEvaluationException
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
		public static <V extends AttributeValue> boolean eval(V arg0, Bag<V> bag)
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
		public PrimitiveToBag(Datatype<AV> paramType, Datatype<Bag<AV>> paramBagType)
		{
			super(paramBagType.getTypeParameter().getFuncIdPrefix() + NAME_SUFFIX_BAG, paramBagType, true, Arrays.asList(paramType));
			this.paramType = paramType;
		}

		@Override
		public FirstOrderFunctionCall<Bag<AV>> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerSinglePrimitiveTypeEval<Bag<AV>, AV>(functionSignature, argExpressions, remainingArgTypes)
			{

				@Override
				protected Bag<AV> evaluate(Deque<AV> args) throws IndeterminateEvaluationException
				{
					return Bags.getInstance(paramType, args);
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

		private static List<? extends Datatype<?>> validate(List<? extends Datatype<?>> parameterTypes)
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
		public SetFunction(String functionIdSuffix, Datatype<RETURN> returnType, boolean varArgs, List<? extends Datatype<Bag<AV>>> parameterTypes)
		{
			super(validate(parameterTypes).get(0).getTypeParameter().getFuncIdPrefix() + functionIdSuffix, returnType, varArgs, parameterTypes);
		}

		@Override
		public final FirstOrderFunctionCall<RETURN> newCall(List<Expression<?>> argExpressions, Datatype<?>... remainingArgTypes) throws IllegalArgumentException
		{
			return new EagerBagEval<RETURN, AV>(functionSignature, argExpressions)
			{

				@Override
				protected RETURN evaluate(Bag<AV>[] bagArgs) throws IndeterminateEvaluationException
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
		public Intersection(Datatype<AV> paramType, Datatype<Bag<AV>> paramBagType)
		{
			super(NAME_SUFFIX_INTERSECTION, paramBagType, false, Arrays.asList(paramBagType, paramBagType));
			this.paramType = paramType;
		}

		@Override
		protected Bag<AV> eval(Bag<AV>[] bagArgs)
		{
			return Bags.getInstance(this.paramType, eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> Set<V> eval(Bag<V> bag0, Bag<V> bag1)
		{
			// http://tekmarathon.com/2012/11/26/find-intersection-of-elements-in-two-arrays/
			// We use a Set because no duplicate shall exist in the result
			final Set<V> intersection = new HashSet<>();
			final Bag<V> smallerBag;
			final Bag<V> biggerBag;
			final int bag0size = bag0.size();
			final int bag1size = bag1.size();
			if (bag0size < bag1size)
			{
				smallerBag = bag0;
				biggerBag = bag1;
			} else
			{
				smallerBag = bag1;
				biggerBag = bag0;
			}

			// for each value in biggest bag, check whether it is in the smaller bag
			for (final V v : biggerBag)
			{
				if (smallerBag.contains(v))
				{
					intersection.add(v);
				}
			}

			return intersection;
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
		public AtLeastOneMemberOf(Datatype<Bag<AV>> paramBagType)
		{
			super(NAME_SUFFIX_AT_LEAST_ONE_MEMBER_OF, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramBagType, paramBagType));
		}

		@Override
		protected BooleanValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(Bag<V> bag0, Bag<V> bag1)
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
		public Union(Datatype<AV> paramType, Datatype<Bag<AV>> paramBagType)
		{
			/*
			 * Union function takes two or more parameters, i.e. two parameters of a specific bag type and a variable-length (zero-to-any) parameter of the same
			 * bag type
			 */
			super(NAME_SUFFIX_UNION, paramBagType, true, Arrays.asList(paramBagType, paramBagType, paramBagType));
			this.paramType = paramType;
		}

		@Override
		protected Bag<AV> eval(Bag<AV>[] bags)
		{
			final Set<AV> result = new HashSet<>();
			for (final Bag<AV> bag : bags)
			{
				for (final AV bagVal : bag)
				{
					result.add(bagVal);
				}
			}

			return Bags.getInstance(this.paramType, result);
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
		public Subset(Datatype<Bag<AV>> paramBagType)
		{
			super(NAME_SUFFIX_SUBSET, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramBagType, paramBagType));
		}

		@Override
		protected BooleanValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(Bag<V> bag0, Bag<V> bag1)
		{
			for (final V v : bag0)
			{
				if (!bag1.contains(v))
				{
					return false;
				}
			}

			return true;
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
		public SetEquals(Datatype<Bag<AV>> paramBagType)
		{
			super(NAME_SUFFIX_SET_EQUALS, StandardDatatypes.BOOLEAN_FACTORY.getDatatype(), false, Arrays.asList(paramBagType, paramBagType));
		}

		@Override
		protected BooleanValue eval(Bag<AV>[] bagArgs)
		{
			return BooleanValue.valueOf(eval(bagArgs[0], bagArgs[1]));
		}

		private static <V extends AttributeValue> boolean eval(Bag<V> bag0, Bag<V> bag1)
		{
			final Set<V> set0 = new HashSet<>();
			for (final V v : bag0)
			{
				set0.add(v);
			}

			final Set<V> set1 = new HashSet<>();
			for (final V v : bag1)
			{
				set1.add(v);
			}

			return set0.equals(set1);
		}

	}

	/**
	 * Creates/gets all first-order bag functions taking a given primitive datatype as bag's primitive type, i.e. the equivalents of standard
	 * <code>urn:oasis:names:tc:xacml:x.x:function:type${suffix}</code> functions, where <code>urn:oasis:names:tc:xacml:x.x:function:type</code> is replaced by
	 * a given prefix ({@code functionIdPrefix} ) and the suffix takes the following values (one per created function):
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
	 * @param paramTypeFactory
	 *            parameter datatype factory
	 * @return first-order bag functions taking the given primitive datatype as bag's primitive type
	 */
	public static <AV extends AttributeValue> Set<Function<?>> getFunctions(DatatypeFactory<AV> paramTypeFactory)
	{
		final Datatype<AV> paramType = paramTypeFactory.getDatatype();
		final BagDatatype<AV> paramBagType = paramTypeFactory.getBagDatatype();
		final Class<AV[]> paramArrayClass = paramTypeFactory.getArrayClass();
		final Set<Function<?>> mutableSet = new HashSet<>();
		/**
		 * 
		 * Single-bag function group, i.e. group of bag functions that takes only one bag as parameter, or no bag parameter but returns a bag. Defined in
		 * section A.3.10. As opposed to Set functions that takes multiple bags as parameters.
		 * 
		 */
		mutableSet.add(new SingletonBagToPrimitive<>(paramType, paramBagType));
		mutableSet.add(new BagSize<>(paramBagType));
		mutableSet.add(new BagContains<>(paramType, paramBagType, paramArrayClass));
		mutableSet.add(new PrimitiveToBag<>(paramType, paramBagType));
		/**
		 * 
		 * Add bag functions that takes multiple bags as parameters. Defined in section A.3.11.
		 * 
		 */
		mutableSet.add(new Intersection<>(paramType, paramBagType));
		mutableSet.add(new AtLeastOneMemberOf<>(paramBagType));
		mutableSet.add(new Union<>(paramType, paramBagType));
		mutableSet.add(new Subset<>(paramBagType));
		mutableSet.add(new SetEquals<>(paramBagType));

		return mutableSet;
	}

}
