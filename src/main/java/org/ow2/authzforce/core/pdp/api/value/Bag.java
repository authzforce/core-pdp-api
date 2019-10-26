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
package org.ow2.authzforce.core.pdp.api.value;

import java.util.Iterator;
import java.util.Objects;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

/**
 * Bag of values (elements) as defined in §7.3.2 of XACML core specification (Attribute bags): <i>The values in a bag are not ordered, and some of the values may be duplicates. There SHALL be no
 * notion of a bag containing bags, or a bag containing values of differing types; i.e., a bag in XACML SHALL contain only values that are of the same data-type</i>. Note that this is consistent with
 * the mathematical definition of a bag a.k.a. multiset.
 * <p>
 * All implementations of this interface must override all the methods of this class except the final ones (this class only throws {@link UnsupportedOperationException} for these), and guarantee the
 * immutability of their bag instances. In particular, {@link #iterator()} must return an immutable {@link java.util.Iterator} ( {@link java.util.Iterator#remove() not supported}. It is required to
 * ensure that values of a given attribute remain constant during an evaluation of a request, as mandated by the XACML spec, section 7.3.5:
 * </p>
 * <p>
 * <i> "Regardless of any dynamic modifications of the request context during policy evaluation, the PDP SHALL behave as if each bag of attribute values is fully populated in the context before it is
 * first tested, and is thereafter immutable during evaluation. (That is, every subsequent test of that attribute shall use the same bag of values that was initially tested.)" </i>
 * </p>
 * <p>
 * {@link #equals(Object)} are implemented according to the mathematical definition of bag/multiset, and {@link #hashCode()} accordingly. Note that multiplicity matters in multisets, therefore this is
 * different from XACML set-equals function which ignores duplicates.
 * </p>
 * <p>
 * NB for developers: we could make this class abstract and let subclasses implement methods except the ones with 'final' modifier. However, we need a common Bag superclass (esp. for internal
 * subclasses in {@link Bags}) that is concrete, in order to use it as the {@link Class} instance returned by {@link BagDatatype#getClass()} and be able to use it in {@link BagDatatype#cast(Value)} to
 * cast any bag instance.
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
		 *             if bag validation fails
		 */
		void validate(Bag<?> bag) throws IndeterminateEvaluationException;

	}

	private final Datatype<AV> elementDatatype;

	/*
	 * We need to make sure that elements cannot be modified. In particular, using Collections.unmodifiableCollection(values) is a bad idea here, because the result (UnmodifiableCollection class) does
	 * not override Object#hashCode() and Object#equals(). But we want deeper equals, i.e. take internal values of collection into account for hashCode() and equals().
	 */
	private final ImmutableMultiset<AV> elements;

	// cached toString()/hashCode() results
	private volatile int hashCode = 0;
	// cached toString() result
	private volatile String toString = null; // Effective Java - Item 71

	/**
	 * Constructor
	 * 
	 * @param elementDatatype
	 *            bag element datatype (non-null)
	 * @param elements
	 *            bag elements (non-null)
	 */
	protected Bag(final Datatype<AV> elementDatatype, final ImmutableMultiset<AV> elements)
	{
		assert elementDatatype != null && elements != null;
		this.elementDatatype = elementDatatype;
		this.elements = elements;
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
	 * {@link #equals(Object)} compares the element datatypes, and calls {@link #equals(Object)} on the results of {@link #elements()}, therefore {@link Multiset#equals(Object)}, which complies with
	 * the mathematical definition of multisets and XACML spec for bags. Note that this is different from XACML set-equals function which does not consider the multiplicity of elements like multisets.
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
		return this.elementDatatype.equals(otherBag.elementDatatype) && elements.equals(otherBag.elements);
	}

	/**
	 * Override Object#hashCode() to apply XACML spec §7.3.2: "The values in a bag are not ordered, and some of the values may be duplicates"
	 */
	@Override
	public final int hashCode()
	{
		// the values in
		if (hashCode == 0)
		{
			hashCode = Objects.hash(this.elementDatatype, this.elements);
		}
		return hashCode;
	}

	/**
	 * Returns true iff the bag contains no value
	 * 
	 * @return true iff the bag contains no value
	 */
	public final boolean isEmpty()
	{
		return elements.isEmpty();
	}

	/**
	 * Get bag size
	 * 
	 * @return bag size
	 */
	public final int size()
	{
		return elements.size();
	}

	/**
	 * Returns true if this bag contains the specified element. More formally, returns true if and only if this bag contains at least one element e such that (v==null ? e==null : v.equals(e)).
	 * 
	 * @param v
	 *            element whose presence in this bag is to be tested
	 * @return true if this collection contains the specified element
	 */
	public final boolean contains(final AV v)
	{
		return elements.contains(v);
	}

	@Override
	public final Iterator<AV> iterator()
	{
		return elements.iterator();
	}

	@Override
	public final String toString()
	{
		// immutable class -> cache this method result
		if (toString == null)
		{
			toString = "Bag(elementType='" + getElementDatatype() + "', elements=" + elements() + ", causeForEmpty=" + getReasonWhyEmpty() + ")";
		}

		return toString;
	}

	/**
	 * Get all elements in the bag.
	 * <p>
	 * Beware the <b>non-null</b>: implementations must return an empty multiset and not null if the bag is empty.
	 * 
	 * @return all elements as a <b>non-null</b> multiset
	 */
	public final Multiset<AV> elements()
	{
		return elements;
	}

	/**
	 * Get the single element in the bag if it is a singleton
	 * 
	 * @return the one-and-only one element in the bag; null if bag is empty or contains multiple elements
	 */
	public AV getSingleElement()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	/**
	 * Get the reason why {@link #isEmpty()} returns true iff it does; or null if it doesn't or if reason is unknown.
	 * 
	 * @return reason why the bag is empty, if it is.
	 * 
	 *         NB: cannot be declared static because overridden by various empty Bag subclasses using member attributes
	 */
	@SuppressWarnings("static-method")
	public IndeterminateEvaluationException getReasonWhyEmpty()
	{
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

}