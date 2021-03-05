/*
 * Copyright 2012-2021 THALES.
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
package org.ow2.authzforce.core.pdp.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * Growable/updatable attribute bag, i.e. mutable bag of attribute values to which you can add as many values as you can. Used only when the total number of values for a given attribute - typically in
 * a XACML request - is not known in advance. For example, for the same AttributeId (e.g. with Issuer = null), there might be multiple <Attribute> elements, in which case values must be merged for
 * later matching <AttributeDesignator> evaluation. Indeed, as discussed on the xacml-dev mailing list (see https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html), the following excerpt
 * from the XACML 3.0 core spec, §7.3.3, indicates that multiple occurrences of the same <Attribute> with same meta-data but different values should be considered equivalent to a single <Attribute>
 * element with same meta-data and merged values (multi-valued Attribute). Moreover, the conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in
 * a multi-value bag during evaluation of the AttributeDesignator.
 * <p>
 * To be instantiated only in a given evaluation request context (handled by a single thread), otherwise not guaranteed thread-safe.
 * 
 * 
 * @param <AV>
 *            element type (primitive). Indeed, XACML spec says for Attribute Bags (7.3.2): "There SHALL be no notion of a bag containing bags, or a bag containing values of differing types; i.e., a
 *            bag in XACML SHALL contain only values that are of the same data-type."
 */
public final class MutableAttributeBag<AV extends AttributeValue> implements Iterable<AV>
{
	private static final IllegalArgumentException NULL_ATTRIBUTE_SOURCE_EXCEPTION = new IllegalArgumentException("Undefined attribute source");

	private static final IllegalArgumentException NULL_DATATYPE_FACTORY_EXCEPTION = new IllegalArgumentException("Undefined attribute datatype");

	private static final UnsupportedOperationException UNSUPPORTED_ADD_OPERATION_EXCEPTION = new UnsupportedOperationException(
	        "Operation forbidden: immutable bag (toImmutable() method already called)");

	private static final IllegalArgumentException ILLEGAL_ATTRIBUTE_VALUE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined attribute value");

	private final List<AV> vals = new ArrayList<>();

	private final Datatype<AV> elementType;

	private volatile AttributeBag<AV> immutableCopy = null;

	private final AttributeSource attributeSource;

	/**
	 * @param elementDatatype
	 *            primitive datatype factory to create every element/value in the bag
	 * @param attributeSource
	 *            attribute bag source
	 * @throws IllegalArgumentException
	 *             iff {@code elementDatatypeFactory == null || attributeSource == null}
	 */
	public MutableAttributeBag(final Datatype<AV> elementDatatype, final AttributeSource attributeSource) throws IllegalArgumentException
	{
		if (elementDatatype == null)
		{
			throw NULL_DATATYPE_FACTORY_EXCEPTION;
		}

		if (attributeSource == null)
		{
			throw NULL_ATTRIBUTE_SOURCE_EXCEPTION;
		}

		this.elementType = elementDatatype;
		this.attributeSource = attributeSource;
	}

	/**
	 * Returns bag element datatype
	 * 
	 * @return the elementType
	 */
	public Datatype<AV> getElementType()
	{
		return elementType;
	}

	/**
	 * Adds value to bag
	 * 
	 * @param value
	 *            attribute value
	 * @throws IllegalArgumentException
	 *             if {@code value == null}
	 */
	public void add(final AV value)
	{
		if (value == null)
		{
			throw ILLEGAL_ATTRIBUTE_VALUE_ARGUMENT_EXCEPTION;
		}

		vals.add(value);
	}

	/**
	 * Adds value to bag
	 * 
	 * @param value
	 *            AttributeValue from a XACML Attribute element
	 * @return value as AV
	 * @throws IllegalArgumentException
	 *             if {@code value == null} or if the datatype of {@code value} is different from other(s) in the attribute bag
	 */
	public AV addRaw(final AttributeValue value) throws IllegalArgumentException
	{
		if (immutableCopy != null)
		{
			throw UNSUPPORTED_ADD_OPERATION_EXCEPTION;
		}

		if (value == null)
		{
			throw ILLEGAL_ATTRIBUTE_VALUE_ARGUMENT_EXCEPTION;
		}

		final AV av;
		try
		{
			av = this.elementType.cast(value);
		} catch (final ClassCastException e)
		{
			throw new IllegalArgumentException(
			        "Invalid datatype of AttributeValue in Attribute element. Expected: " + elementType + " (datatype of other value(s) already found in the same attribute bag)");
		}

		vals.add(av);
		return av;
	}

	/**
	 * Lock the bag and return the immutable copy of this bag
	 * 
	 * @return immutable bag
	 */
	public AttributeBag<AV> toImmutable()
	{
		if (this.immutableCopy == null)
		{
			immutableCopy = Bags.newAttributeBag(elementType, vals, attributeSource);
		}

		return this.immutableCopy;
	}

	/**
	 * Appends all of the elements in the specified collection to the end of this bag, in the order that they are returned by the specified collection's iterator (optional operation). The behavior of
	 * this operation is undefined if the specified collection is modified while the operation is in progress. (Note that this will occur if the specified collection is this list, and it's nonempty.)
	 * 
	 * @param list
	 *            collection containing elements to be added to this list
	 * @throws IllegalArgumentException
	 *             if {@code list} is null
	 * @throws ClassCastException
	 *             if one of the values in {@code list} is not assignable to the type {@code AV}
	 */
	public void addAll(final Collection<? extends AttributeValue> list) throws IllegalArgumentException, ClassCastException
	{
		for (final AttributeValue newVal : list)
		{
			this.vals.add(this.elementType.cast(newVal));
		}
	}

	@Override
	public Iterator<AV> iterator()
	{
		return this.toImmutable().iterator();
	}

}