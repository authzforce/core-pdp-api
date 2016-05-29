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
import java.util.Collections;
import java.util.Iterator;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * This class consists exclusively of static methods that operate on or return {@link Bag}s. NOTE: do not merge this into {@link Bag} at risk of violating the
 * Acyclic Dependencies principle.
 *
 */
public final class Bags
{
	private Bags()
	{
	}

	private static final class Empty<AV extends AttributeValue> extends Bag<AV>
	{
		private Empty(Datatype<AV> elementDatatype, IndeterminateEvaluationException causeForEmpty) throws IllegalArgumentException
		{
			// Collections.emptySet() returns immutable Set
			super(elementDatatype, Collections.<AV> emptySet(), causeForEmpty);
		}
	}

	private static final class Singleton<AV extends AttributeValue> extends Bag<AV>
	{
		private Singleton(Datatype<AV> elementDatatype, AV val) throws IllegalArgumentException
		{
			// Collections.singleton() returns immutable Set
			super(elementDatatype, Collections.singleton(val), null);
		}
	}

	private static final class Multi<AV extends AttributeValue> extends Bag<AV>
	{
		private Multi(Datatype<AV> elementDatatype, Collection<AV> values) throws IllegalArgumentException
		{
			super(elementDatatype, Collections.unmodifiableCollection(values), null);
		}

		/**
		 * Override equals() to take internal values into account, because the internal Collections.unmodifiableCollection(...) does not do it
		 */
		@Override
		public boolean equals(Object other)
		{
			// Effective Java - Item 8
			if (this == other)
			{
				return true;
			}

			if (!(other instanceof Bag))
			{
				return false;
			}

			final Bag<?> otherBag = (Bag<?>) other;
			if (!this.elementDatatype.equals(otherBag.elementDatatype))
			{
				return false;
			}

			final Iterator<AV> thisIterator = iterator();
			final Iterator<? extends AttributeValue> otherIterator = otherBag.iterator();
			while (thisIterator.hasNext() && otherIterator.hasNext())
			{
				final AV o1 = thisIterator.next();
				final AttributeValue o2 = otherIterator.next();
				if (!(o1 == null ? o2 == null : o1.equals(o2)))
				{
					return false;
				}
			}

			return !thisIterator.hasNext() && !otherIterator.hasNext();
		}

		/**
		 * Override hashCode() to take internal values into account, because the internal Collections.unmodifiableCollection(...) does not do it
		 */
		@Override
		public int hashCode()
		{
			if (hashCode == 0)
			{
				hashCode = 1;
				for (final AV e : this)
				{
					hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
				}
			}

			return hashCode;
		}
	}

	private static final IllegalArgumentException NULL_BAG_ELEMENT_EXCEPTION = new IllegalArgumentException("Null value in bag");

	/**
	 * Creates instance of empty bag with given exception as reason for bag being empty (no attribute value), e.g. error occurred during evaluation
	 * 
	 * @param causeForEmpty
	 *            reason for empty bag (optional but should be specified whenever possible, to help troubleshoot)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> empty(Datatype<AV> elementDatatype, IndeterminateEvaluationException causeForEmpty)
			throws IllegalArgumentException
	{
		return new Empty<>(elementDatatype, causeForEmpty);
	}

	/**
	 * Creates instance of bag containing val and only val value
	 * 
	 * @param elementDatatype
	 *            bag element datatype
	 * @param val
	 *            the val and only val value in the bag
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code val == null || elementDatatype == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> singleton(Datatype<AV> elementDatatype, AV val) throws IllegalArgumentException
	{
		if (val == null)
		{
			throw NULL_BAG_ELEMENT_EXCEPTION;
		}

		return new Singleton<>(elementDatatype, val);
	}

	/**
	 * Creates instance of bag of values.
	 * 
	 * @param values
	 *            bag values, typically a List for ordered results, e.g. attribute values for which order matters; or it may be a Set for result of bag/Set
	 *            functions (intersection, union...)
	 * @param elementDatatype
	 *            bag element datatype
	 * @return bag
	 * @throws IllegalArgumentException
	 *             if {@code elementDatatype == null } or {@code values} has at least one element which is null:
	 *             {@code values != null && !values.isEmpty() && values.iterator().next() == null}
	 */
	public static <AV extends AttributeValue> Bag<AV> getInstance(Datatype<AV> elementDatatype, Collection<AV> values) throws IllegalArgumentException
	{
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
}
