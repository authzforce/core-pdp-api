/**
 * Copyright 2012-2018 THALES.
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
package org.ow2.authzforce.core.pdp.api.io;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.MutableAttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.PrimitiveValue;

/**
 * 
 * Internal equivalent of XACML Attributes element, i.e. attributes specific to a single category
 * 
 * @param <AV_BAG>
 *            type of bag of attribute values
 * @param <RETURNED_ATTRIBUTE_CATEGORY>
 *            type of corresponding attribute Category output containing only the subset of this category-specific attributes that had IncludeInResult = 'true' in the XACML request
 * 
 */
public final class SingleCategoryAttributes<AV_BAG extends Iterable<? extends AttributeValue>, RETURNED_ATTRIBUTE_CATEGORY> implements Iterable<Entry<AttributeFqn, AttributeBag<?>>>
{

	private static final UnsupportedOperationException UNSUPPORTED_ITERATOR_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"SingleCategoryAttributes - named attributes iterator may be called only once.");

	/**
	 * Named Attribute Iterator converter
	 *
	 * @param <V_BAG>
	 *            type of resulting named attribute iterator after conversion
	 */
	public interface NamedAttributeIteratorConverter<V_BAG extends Iterable<? extends AttributeValue>>
	{
		/**
		 * convert the iterator over named attributes
		 * 
		 * @param namedAttributeIterator
		 * @return new named attribute iterator
		 */
		Iterator<Entry<AttributeFqn, AttributeBag<?>>> convert(Iterator<Entry<AttributeFqn, V_BAG>> namedAttributeIterator);
	}

	private static final class MutableBagBasedImmutableIterator implements Iterator<Entry<AttributeFqn, AttributeBag<?>>>
	{

		private static final UnsupportedOperationException UNSUPPORTED_ITERATOR_REMOVE_OPERATION_EXCEPTION = new UnsupportedOperationException("Cannot remove element via Immutable iterator");
		private final Iterator<Entry<AttributeFqn, MutableAttributeBag<?>>> mutableIterator;

		private MutableBagBasedImmutableIterator(final Iterator<Entry<AttributeFqn, MutableAttributeBag<?>>> mutableIterator)
		{
			this.mutableIterator = mutableIterator;
		}

		@Override
		public boolean hasNext()
		{
			return this.mutableIterator.hasNext();
		}

		@Override
		public Entry<AttributeFqn, AttributeBag<?>> next()
		{
			final Entry<AttributeFqn, MutableAttributeBag<?>> entry = this.mutableIterator.next();
			return new SimpleEntry<>(entry.getKey(), entry.getValue().toImmutable());
		}

		@Override
		public void remove()
		{
			throw UNSUPPORTED_ITERATOR_REMOVE_OPERATION_EXCEPTION;
		}

	}

	/**
	 * Attribute Iterator Converter for {@link MutableAttributeBag}
	 */
	public static final NamedAttributeIteratorConverter<MutableAttributeBag<?>> MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER = new NamedAttributeIteratorConverter<MutableAttributeBag<?>>()
	{

		@Override
		public Iterator<Entry<AttributeFqn, AttributeBag<?>>> convert(final Iterator<Entry<AttributeFqn, MutableAttributeBag<?>>> namedAttributeIterator)
		{
			return new MutableBagBasedImmutableIterator(namedAttributeIterator);
		}

	};

	/**
	 * "Identity" Attribute Iterator Converter, i.e. returns the iterator in argument as is ("identity" as in mathematical definition of identity function/transformation)
	 */
	public static final NamedAttributeIteratorConverter<AttributeBag<?>> IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER = new NamedAttributeIteratorConverter<AttributeBag<?>>()
	{

		@Override
		public Iterator<Entry<AttributeFqn, AttributeBag<?>>> convert(final Iterator<Entry<AttributeFqn, AttributeBag<?>>> namedAttributeIterator)
		{
			return namedAttributeIterator;
		}

	};

	private interface IteratorProvider<AV_BAG extends Iterable<? extends PrimitiveValue>>
	{
		Iterator<Entry<AttributeFqn, AttributeBag<?>>> get(Set<Entry<AttributeFqn, AV_BAG>> namedAttributes);
	}

	private static final IteratorProvider<Bag<? extends AttributeValue>> EMPTY_ITERATOR_PROVIDER = new IteratorProvider<Bag<? extends AttributeValue>>()
	{

		@Override
		public Iterator<Entry<AttributeFqn, AttributeBag<?>>> get(final Set<Entry<AttributeFqn, Bag<? extends AttributeValue>>> namedAttributeIterator)
		{
			return Collections.<Entry<AttributeFqn, AttributeBag<?>>> emptyIterator();
		}

	};

	private static final class ConvertingIteratorProvider<AV_BAG extends Iterable<? extends AttributeValue>> implements IteratorProvider<AV_BAG>
	{
		private final NamedAttributeIteratorConverter<AV_BAG> namedAttributeIteratorConverter;

		private ConvertingIteratorProvider(final NamedAttributeIteratorConverter<AV_BAG> namedAttributeIteratorConverter)
		{
			assert namedAttributeIteratorConverter != null;
			this.namedAttributeIteratorConverter = namedAttributeIteratorConverter;
		}

		@Override
		public Iterator<Entry<AttributeFqn, AttributeBag<?>>> get(final Set<Entry<AttributeFqn, AV_BAG>> namedAttributes)
		{
			assert namedAttributes != null;
			return namedAttributeIteratorConverter.convert(namedAttributes.iterator());
		}
	}

	private final String categoryId;
	private final Set<Entry<AttributeFqn, AV_BAG>> namedAttributes;
	private final IteratorProvider<AV_BAG> iteratorProvider;
	private final RETURNED_ATTRIBUTE_CATEGORY attrsToIncludeInResult;

	/*
	 * Corresponds to Attributes/Content marshalled to XPath data model for XPath evaluation (e.g. AttributeSelector or XPath-based evaluation). This is set to null if no Content provided or no
	 * feature using XPath evaluation against Content is enabled.
	 */
	private final XdmNode extraContent;

	private volatile boolean iteratorCalled = false;

	/**
	 * Instantiates this class
	 * 
	 * @param categoryId
	 *            category ID
	 * 
	 * @param namedAttributes
	 *            Named attributes (in the XACML sense) where each entry consists of the identifier of the attribute and its value bag
	 * @param namedAttributeIteratorConverter
	 *            converts the iterator of {@code namedAttributes} into constant-valued attribute iterator
	 * @param returnedAttributeCategory
	 *            corresponding attribute Category output containing only the subset of this category-specific attributes that had IncludeInResult = 'true' in the XACML request
	 * @param extraContent
	 *            Attributes/Content parsed into XPath data model for XPath evaluation
	 * @throws IllegalArgumentException
	 *             iff {@code namedAttributes != null && !namedAttributes.isEmpty() && namedAttributeIteratorConverter == null} (namedAttributeIteratorConverter required if namedAttributes not
	 *             null/empty)
	 */
	public SingleCategoryAttributes(final String categoryId, final Set<Entry<AttributeFqn, AV_BAG>> namedAttributes, final NamedAttributeIteratorConverter<AV_BAG> namedAttributeIteratorConverter,
			final RETURNED_ATTRIBUTE_CATEGORY returnedAttributeCategory, final XdmNode extraContent) throws IllegalArgumentException
	{
		this.categoryId = categoryId;
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
				throw new IllegalArgumentException("Null input namedAttributeIteratorConverter but required because namedAttributes not null/empty: " + namedAttributes);
			}

			this.namedAttributes = namedAttributes;
			this.iteratorProvider = new ConvertingIteratorProvider<>(namedAttributeIteratorConverter);
		}

		this.attrsToIncludeInResult = returnedAttributeCategory;
		this.extraContent = extraContent;
	}

	/**
	 * Gets the category ID
	 * 
	 * @return category ID
	 */
	public String getCategoryId()
	{
		return this.categoryId;
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
	 * Get corresponding attribute Category output containing only the subset of this category-specific attributes that had IncludeInResult = 'true' in the XACML request
	 * 
	 * @return the attributes to include in the final Result; null if nothing to include
	 */
	public RETURNED_ATTRIBUTE_CATEGORY getAttributesToIncludeInResult()
	{
		return attrsToIncludeInResult;
	}

	@Override
	public Iterator<Entry<AttributeFqn, AttributeBag<?>>> iterator()
	{
		if (iteratorCalled)
		{
			throw UNSUPPORTED_ITERATOR_OPERATION_EXCEPTION;
		}

		return this.iteratorProvider.get(namedAttributes);
	}
}