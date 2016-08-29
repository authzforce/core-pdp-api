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

import java.util.Iterator;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

import com.koloboke.collect.set.hash.HashObjSets;

/**
 * Bag of values (elements) as defined in §7.3.2 of XACML core specification (Attribute bags): <i>The values in a bag
 * are not ordered, and some of the values may be duplicates. There SHALL be no notion of a bag containing bags, or a
 * bag containing values of differing types; i.e., a bag in XACML SHALL contain only values that are of the same
 * data-type</i>
 * <p>
 * All implementations of this interface must override all the methods of this class except the final ones (this class
 * only throws {@link UnsupportedOperationException} for these), and guarantee the immutability of their bag instances.
 * In particular, {@link #iterator()} must return an immutable {@link java.util.Iterator}
 * ({@link java.util.Iterator#remove() not supported}. It is required to ensure that values of a given attribute remain
 * constant during an evaluation of a request, as mandated by the XACML spec, section 7.3.5:
 * </p>
 * <p>
 * <i> "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as
 * if each bag of attribute values is fully populated in the context before it is first tested, and is thereafter
 * immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that
 * was initially tested.)" </i>
 * </p>
 * <p>
 * {@link #equals(Object)} are implemented according to XACML definition of set-equals function, i.e. ignoring
 * duplicates, and {@link #hashCode()} accordingly.
 * </p>
 * <p>
 * NB for developers: we could make this class abstract and let subclasses implement methods except the ones with
 * 'final' modifier. However, we need a common Bag superclass (esp. for internal subclasses in {@link Bags}) that is
 * concrete, in order to use it as the {@link Class} instance returned by {@link BagDatatype#getClass()} and be able to
 * use it in {@link BagDatatype#cast(Value)} to cast any bag instance.
 * </p>
 * 
 * @param <AV>
 *            type of every element in the bag
 */
public class Bag<AV extends AttributeValue> implements Value, Iterable<AV>
{
	private static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();

	/**
	 * Bag content validator
	 */
	public interface Validator
	{

		/**
		 * Validates the bag content, e.g. that it is not empty
		 * 
		 * @param bag
		 *            input bag
		 * @throws IndeterminateEvaluationException
		 *             if validation fails
		 */
		void validate(Bag<?> bag) throws IndeterminateEvaluationException;

	}

	private final Datatype<AV> elementDatatype;

	// cached toString()/hashCode() results
	private volatile int hashCode = 0;

	protected Bag(Datatype<AV> elementDatatype)
	{
		assert elementDatatype != null;

		this.elementDatatype = elementDatatype;
	}

	/**
	 * Get this bag's element datatype (datatype of every element in the bag)
	 *
	 * @return this bag's element datatype
	 */
	public final Datatype<AV> getElementDatatype()
	{
		return this.elementDatatype;
	}

	/**
	 * Override Object#equals() to apply XACML spec §7.3.2: "The values in a bag are not ordered, and some of the values
	 * may be duplicates". Implemented according to XACML definition of set-equals
	 */
	@Override
	public final boolean equals(final Object other)
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
		if (!this.elementDatatype.equals(otherBag.getElementDatatype()))
		{
			return false;
		}

		/*
		 * XACML spec §7.3.2: "The values in a bag are not ordered, and some of the values may be duplicates".
		 */
		/*
		 * Check first that otherBag is a subset of this (this contains all otherBag values)
		 */
		final Iterator<? extends AttributeValue> otherIterator = otherBag.iterator();
		while (otherIterator.hasNext())
		{
			final AttributeValue otherVal = otherIterator.next();
			if (!contains(elementDatatype.cast(otherVal)))
			{
				return false;
			}
		}

		/*
		 * Check that this is a subset of otherBag (otherBag contains all values of this)
		 */
		final Bag<AV> otherBagOfAV = (Bag<AV>) otherBag;
		final Iterator<AV> thisIterator = this.iterator();
		while (thisIterator.hasNext())
		{
			if (!otherBagOfAV.contains(thisIterator.next()))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Override Object#hashCode() to apply XACML spec §7.3.2: "The values in a bag are not ordered, and some of the
	 * values may be duplicates"
	 */
	@Override
	public final int hashCode()
	{
		// the values in
		if (hashCode == 0)
		{
			if (isEmpty())
			{
				hashCode = 1;
			}
			else
			{
				// bag is not empty
				final Iterator<AV> i = iterator();
				final AV val0 = i.next();
				/*
				 * XACML spec §7.3.2: "The values in a bag are not ordered". Also equals() ignores duplicates. So we
				 * handle them like Java Set.
				 */
				hashCode += val0.hashCode();

				if (i.hasNext())
				{
					final Set<AV> setView = HashObjSets.newUpdatableSet(size());
					setView.add(val0);
					do
					{
						final AV obj = i.next();
						if (obj != null && setView.add(obj))
						{
							/*
							 * XACML spec §7.3.2: "The values in a bag are not ordered". Also equals() ignores
							 * duplicates. So we handle them like Java Set.
							 */
							hashCode += obj.hashCode();
						}
					} while (i.hasNext());
				}
			}
		}
		return hashCode;
	}

	/**
	 * Returns true iff the bag contains no value
	 * 
	 * @return true iff the bag contains no value
	 */
	public boolean isEmpty()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/**
	 * Get the reason why {@link #isEmpty()} returns true iff it does; or null if it doesn't or if reason is unknown.
	 * 
	 * @return reason why the bag is empty, if it is
	 */
	public IndeterminateEvaluationException getReasonWhyEmpty()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/**
	 * Get bag size
	 * 
	 * @return bag size
	 */
	public int size()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/**
	 * Returns true if this bag contains the specified element. More formally, returns true if and only if this bag
	 * contains at least one element e such that (v==null ? e==null : v.equals(e)).
	 * 
	 * @param v
	 *            element whose presence in this bag is to be tested
	 * @return true if this collection contains the specified element
	 */
	public boolean contains(final AV v)
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/**
	 * Get the single value in the bag if it is a singleton
	 * 
	 * @return the one-and-only one value in the bag; null if bag is empty or contains multiple values
	 */
	public AV getSingleValue()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	@Override
	public Iterator<AV> iterator()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

}