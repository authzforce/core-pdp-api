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
package org.ow2.authzforce.core.pdp.api;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

/**
 * 
 * Internal equivalent of XACML Attributes element, i.e. attributes specific to a single category
 * 
 * @param <AV_BAG>
 *            type of bag of attribute values
 * 
 */
public final class SingleCategoryAttributes<AV_BAG extends Iterable<? extends AttributeValue>>
		implements Iterable<Entry<AttributeGUID, Bag<?>>>
{

	private static final UnsupportedOperationException UNSUPPORTED_ITERATOR_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"SingleCategoryAttributes - named attributes iterator may be called only once.");

	interface NamedAttributeIteratorConverter<V_BAG extends Iterable<? extends AttributeValue>>
	{
		Iterator<Entry<AttributeGUID, Bag<?>>> convert(Iterator<Entry<AttributeGUID, V_BAG>> namedAttributeIterator);
	}

	private static final class MutableBagBasedImmutableIterator implements Iterator<Entry<AttributeGUID, Bag<?>>>
	{

		private static final UnsupportedOperationException UNSUPPORTED_ITERATOR_REMOVE_OPERATION_EXCEPTION = new UnsupportedOperationException(
				"Cannot remove element via Immutable iterator");
		private final Iterator<Entry<AttributeGUID, MutableBag<?>>> mutableIterator;

		private MutableBagBasedImmutableIterator(final Iterator<Entry<AttributeGUID, MutableBag<?>>> mutableIterator)
		{
			this.mutableIterator = mutableIterator;
		}

		@Override
		public boolean hasNext()
		{
			return this.mutableIterator.hasNext();
		}

		@Override
		public Entry<AttributeGUID, Bag<?>> next()
		{
			final Entry<AttributeGUID, MutableBag<?>> entry = this.mutableIterator.next();
			return new SimpleEntry<AttributeGUID, Bag<?>>(entry.getKey(), entry.getValue().toImmutable());
		}

		@Override
		public void remove()
		{
			throw UNSUPPORTED_ITERATOR_REMOVE_OPERATION_EXCEPTION;
		}

	}

	/**
	 * Attribute Iterator Converter for {@link MutableBag}
	 */
	public static final NamedAttributeIteratorConverter<MutableBag<?>> MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER = new NamedAttributeIteratorConverter<MutableBag<?>>()
	{

		@Override
		public Iterator<Entry<AttributeGUID, Bag<?>>> convert(
				final Iterator<Entry<AttributeGUID, MutableBag<?>>> namedAttributeIterator)
		{
			return new MutableBagBasedImmutableIterator(namedAttributeIterator);
		}

	};

	/**
	 * "Identity" Attribute Iterator Converter, i.e. returns the iterator in argument as is ("identity" as in
	 * mathematical definition of identity function/transformation)
	 */
	public static final NamedAttributeIteratorConverter<Bag<?>> IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER = new NamedAttributeIteratorConverter<Bag<?>>()
	{

		@Override
		public Iterator<Entry<AttributeGUID, Bag<?>>> convert(
				final Iterator<Entry<AttributeGUID, Bag<?>>> namedAttributeIterator)
		{
			return namedAttributeIterator;
		}

	};

	private interface IteratorProvider<AV_BAG extends Iterable<? extends AttributeValue>>
	{
		Iterator<Entry<AttributeGUID, Bag<?>>> get(Set<Entry<AttributeGUID, AV_BAG>> namedAttributes);
	}

	private static final IteratorProvider<Bag<?>> EMPTY_ITERATOR_PROVIDER = new IteratorProvider<Bag<?>>()
	{

		@Override
		public Iterator<Entry<AttributeGUID, Bag<?>>> get(Set<Entry<AttributeGUID, Bag<?>>> namedAttributeIterator)
		{
			return Collections.<Entry<AttributeGUID, Bag<?>>>emptyIterator();
		}

	};

	private static final class ConvertingIteratorProvider<AV_BAG extends Iterable<? extends AttributeValue>>
			implements IteratorProvider<AV_BAG>
	{
		private final NamedAttributeIteratorConverter<AV_BAG> namedAttributeIteratorConverter;

		private ConvertingIteratorProvider(NamedAttributeIteratorConverter<AV_BAG> namedAttributeIteratorConverter)
		{
			assert namedAttributeIteratorConverter != null;
			this.namedAttributeIteratorConverter = namedAttributeIteratorConverter;
		}

		@Override
		public Iterator<Entry<AttributeGUID, Bag<?>>> get(Set<Entry<AttributeGUID, AV_BAG>> namedAttributes)
		{
			assert namedAttributes != null;
			return namedAttributeIteratorConverter.convert(namedAttributes.iterator());
		}
	}

	private final Set<Entry<AttributeGUID, AV_BAG>> namedAttributes;
	private final IteratorProvider<AV_BAG> iteratorProvider;
	private final Attributes attrsToIncludeInResult;

	/*
	 * Corresponds to Attributes/Content marshalled to XPath data model for XPath evaluation (e.g. AttributeSelector or
	 * XPath-based evaluation). This is set to null if no Content provided or no feature using XPath evaluation against
	 * Content is enabled.
	 */
	private final XdmNode extraContent;

	private volatile boolean iteratorCalled = false;

	/**
	 * Instantiates this class
	 * 
	 * @param namedAttributes
	 *            Named attributes (in the XACML sense) where each entry consists of the identifier of the attribute and
	 *            its value bag
	 * @param namedAttributeIteratorConverter
	 *            converts the iterator of {@code namedAttributes} into constant-valued attribute iterator
	 * @param attributesToIncludeInResult
	 *            Attributes with only the Attribute elements to include in final Result (IncludeInResult = true in
	 *            original XACML request) or null if there was none
	 * @param extraContent
	 *            Attributes/Content parsed into XPath data model for XPath evaluation
	 * @throws IllegalArgumentException
	 *             iff
	 *             {@code namedAttributes != null && !namedAttributes.isEmpty() && namedAttributeIteratorConverter == null}
	 *             (namedAttributeIteratorConverter required if namedAttributes not null/empty)
	 */
	public SingleCategoryAttributes(final Set<Entry<AttributeGUID, AV_BAG>> namedAttributes,
			final NamedAttributeIteratorConverter<AV_BAG> namedAttributeIteratorConverter,
			final Attributes attributesToIncludeInResult, final XdmNode extraContent) throws IllegalArgumentException
	{
		// Reminder: XACML <Attribute> element is not mandatory in XACML <Attributes>
		if (namedAttributes == null || namedAttributes.isEmpty())
		{
			this.namedAttributes = Collections.emptySet();
			this.iteratorProvider = (IteratorProvider<AV_BAG>) EMPTY_ITERATOR_PROVIDER;
		}
		else
		{
			if (namedAttributeIteratorConverter == null)
			{
				throw new IllegalArgumentException(
						"Null input namedAttributeIteratorConverter but required because namedAttributes not null/empty: "
								+ namedAttributes);
			}

			this.namedAttributes = namedAttributes;
			this.iteratorProvider = new ConvertingIteratorProvider<>(namedAttributeIteratorConverter);
		}

		this.attrsToIncludeInResult = attributesToIncludeInResult;
		this.extraContent = extraContent;
	}

	/**
	 * Gets the Content parsed into XPath data model for XPath evaluation; or null if no Content
	 * 
	 * @return the Content in XPath data model
	 */
	public XdmNode getExtraContent()
	{
		return extraContent;
	}

	/**
	 * Get Attributes to include in the final Result (IncludeInResult = true in original XACML request)
	 * 
	 * @return the attributes to include in the final Result; null if nothing to include
	 */
	public Attributes getAttributesToIncludeInResult()
	{
		return attrsToIncludeInResult;
	}

	@Override
	public Iterator<Entry<AttributeGUID, Bag<?>>> iterator()
	{
		if (iteratorCalled)
		{
			throw UNSUPPORTED_ITERATOR_OPERATION_EXCEPTION;
		}

		return this.iteratorProvider.get(namedAttributes);
	}
}