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
package org.ow2.authzforce.core.pdp.api.value;

import java.util.Collection;
import java.util.Iterator;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.StatusHelper;
import org.ow2.authzforce.core.pdp.api.value.Bag.Validator;

import com.google.common.collect.ImmutableMultiset;

/**
 * This class consists exclusively of static methods that operate on or return
 * {@link Bag}s. NOTE: do not merge this into {@link Bag} at risk of violating
 * the Acyclic Dependencies principle.
 *
 */
public final class Bags
{
	private static final IllegalArgumentException NULL_DATATYPE_EXCEPTION = new IllegalArgumentException(
			"Undefined bag datatype argument");
	private static final IllegalArgumentException NULL_BAG_ELEMENT_EXCEPTION = new IllegalArgumentException(
			"Null value in bag");

	/**
	 * Empty bag
	 * 
	 * @param <AV>
	 *            element datatype
	 */
	private static final class Empty<AV extends AttributeValue> extends Bag<AV>
	{
		private final IndeterminateEvaluationException causeForEmpty;

		private Empty(final Datatype<AV> elementDatatype, final IndeterminateEvaluationException causeForEmpty)
		{
			super(elementDatatype, ImmutableMultiset.<AV> of());
			this.causeForEmpty = causeForEmpty;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return this.causeForEmpty;
		}

		@Override
		public AV getSingleElement()
		{
			return null;
		}

	}

	/**
	 * Single-valued bag
	 * 
	 * @param <AV>
	 *            single value datatype
	 */
	private static final class Singleton<AV extends AttributeValue> extends Bag<AV>
	{
		private final AV singleVal;

		private Singleton(final Datatype<AV> elementDatatype, final AV val)
		{
			super(elementDatatype, ImmutableMultiset.of(val));
			assert val != null;

			this.singleVal = val;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return null;
		}

		@Override
		public AV getSingleElement()
		{
			return this.singleVal;
		}
	}

	/**
	 * Multi-valued bag
	 * 
	 * @param <AV>
	 *            element datatype
	 */
	private static final class Multi<AV extends AttributeValue> extends Bag<AV>
	{
		/**
		 * Constructor specifying bag datatype. On the contrary to
		 * {@link #Bag(Datatype)}, this constructor allows to reuse an existing
		 * bag Datatype object, saving the allocation of such object.
		 * 
		 * @param elementDatatype
		 *            element datatype
		 * 
		 * @param values
		 *            bag values (content).
		 * @param causeForEmpty
		 *            reason why this bag is empty if it is; null if it isn't
		 * @throws IllegalArgumentException
		 *             if {@code elementDatatype == null}
		 */
		private Multi(final Datatype<AV> elementDatatype, final Collection<? extends AV> values)
		{
			super(elementDatatype, ImmutableMultiset.copyOf(values));
			assert values != null && values.size() > 1;
		}

		@Override
		public IndeterminateEvaluationException getReasonWhyEmpty()
		{
			return null;
		}

		@Override
		public AV getSingleElement()
		{
			return size() == 1 ? elements().iterator().next() : null;
		}

	}

	/**
	 * Creates instance of immutable empty bag with given exception as reason
	 * for bag being empty (no attribute value), e.g. error occurred during
	 * evaluation
	 * 
	 * @param causeForEmpty
	 *            reason for empty bag (optional but should be specified
	 *            whenever possible, to help troubleshoot)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> empty(final Datatype<AV> elementDatatype,
			final IndeterminateEvaluationException causeForEmpty) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		return new Empty<>(elementDatatype, causeForEmpty);
	}

	/**
	 * Creates instance of immutable bag containing val and only val value
	 * 
	 * @param elementDatatype
	 *            bag element datatype
	 * @param val
	 *            the val and only val value in the bag
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code val == null || elementDatatype == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> singleton(final Datatype<AV> elementDatatype, final AV val)
			throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (val == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		return new Singleton<>(elementDatatype, val);
	}

	/**
	 * Creates instance of immutable bag of values.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g.
	 *            attribute values for which order matters; or it may be a Set
	 *            for result of bag/Set functions (intersection, union...)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null } or {@code values} has at
	 *             least one element which is null:
	 *             {@code values != null && !values.isEmpty() && values.iterator().next() == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> getInstance(final Datatype<AV> elementDatatype,
			final Collection<AV> values) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_EXCEPTION;
		}

		if (values == null || values.isEmpty())
		{
			return new Empty<>(elementDatatype, null);
		}

		final Iterator<AV> valueIterator = values.iterator();
		final AV val0 = valueIterator.next();
		if (val0 == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		if (!valueIterator.hasNext())
		{
			// only one value
			return new Singleton<>(elementDatatype, val0);
		}

		// more than one value
		return new Multi<>(elementDatatype, values);
	}

	/**
	 * Checks the bag is not empty, typically used to enforce MustBePresent=True
	 * on XACML AttributeDesignator/AttributeSelector elements
	 */
	public static final class NonEmptinessValidator implements Validator
	{

		private final String messageIfEmpty;

		/**
		 * Creates validator
		 * 
		 * @param messageIfEmpty
		 *            message used as exception message if bag is empty
		 */
		public NonEmptinessValidator(final String messageIfEmpty)
		{
			this.messageIfEmpty = messageIfEmpty;
		}

		@Override
		public void validate(final Bag<?> bag) throws IndeterminateEvaluationException
		{
			assert bag != null;

			if (bag.isEmpty())
			{
				throw new IndeterminateEvaluationException(messageIfEmpty, StatusHelper.STATUS_MISSING_ATTRIBUTE,
						bag.getReasonWhyEmpty());
			}

		}

	}

	/**
	 * Dumb validator that does nothing, typically used for MustBePresent=False
	 * on XACML AttributeDesignator/AttributeSelector elements
	 */
	public static final Validator DUMB_VALIDATOR = new Validator()
	{

		@Override
		public void validate(final Bag<?> bag)
		{
			// do nothing since the flag is disabled
		}

	};

	private Bags()
	{
	}
}
