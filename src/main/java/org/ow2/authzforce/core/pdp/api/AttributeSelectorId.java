/*
 * Copyright 2012-2023 THALES.
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

import java.util.Objects;
import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;

/**
 * AttributeSelector identifier (category, contextSelectorId, path). Why not use AttributeSelector directly? Because we don't care about MustBePresent or Datatype for lookup here. This is used for
 * example as key in a map to retrieve corresponding AttributeValue when it has already been evaluated.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ContextSelectorId, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] {@literal http://www.w3.org/TR/xmlschema-2/#anyURI}. That's why we use String instead.
 * </p>
 * 
 */
public final class AttributeSelectorId implements Comparable<AttributeSelectorId>
{
	private static final IllegalArgumentException NULL_PATH_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined AttributeSelector Path");
	private static final IllegalArgumentException NULL_CATEGORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined AttributeSelector Category");
	private final String category;
	private final String path;
	private final Optional<String> contextSelectorId;

	// cached method results
	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * Creates instance from scratch
	 *
	 * @param category
	 *            AttributeSelector's Category
	 * @param xpath
	 * 	             AttributeSelector's Path
	 * @param contextSelectorId AttributeSelector's ContextSelectorId
	 */
	public AttributeSelectorId(final String category, final String xpath, final Optional<String> contextSelectorId)
	{
		if (category == null)
		{
			throw NULL_CATEGORY_ARGUMENT_EXCEPTION;
		}

		if (xpath == null)
		{
			throw NULL_PATH_ARGUMENT_EXCEPTION;
		}

		this.category = category;
		this.path = xpath;
		this.contextSelectorId = contextSelectorId;
	}

	/**
	 * Creates instance from XACML AttributeSelector
	 * 
	 * @param attrSelector
	 *            attribute selector
	 */
	public AttributeSelectorId(final AttributeSelectorType attrSelector)
	{
		this(attrSelector.getCategory(), attrSelector.getPath(), Optional.of(attrSelector.getContextSelectorId()));
	}

	/**
	 * @return AttributeSelector Category
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * @return AttributeSelector Path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @return AttributeSelector ContextSelectorId
	 */
	public Optional<String> getContextSelectorId()
	{
		return contextSelectorId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = Objects.hash(category, path, contextSelectorId);
		}

		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		// Effective Java - Item 8
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof AttributeSelectorId other))
		{
			return false;
		}

		return this.category.equals(other.category) && this.path.equals(other.path) && Objects.equals(this.contextSelectorId, other.contextSelectorId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (toString == null)
		{
			toString = "[Category=" + category + ", ContextSelectorId=" + contextSelectorId + ", Path=" + path + "]";
		}

		return toString;
	}

	/**
	 * Compares using lexicographical ordering on Category, then Path, then finally the ContextSelectorId.
	 */
	@Override
	public int compareTo(final AttributeSelectorId other)
	{
		final int thisCatComparedToOtherCat = this.category.compareTo(other.category);
		if (thisCatComparedToOtherCat != 0)
		{
			return thisCatComparedToOtherCat;
		}

		final int thisPathComparedToOtherPath = this.path.compareTo(other.path);
		if (thisPathComparedToOtherPath != 0)
		{
			return thisPathComparedToOtherPath;
		}

		return this.contextSelectorId.map(value -> other.contextSelectorId.map(value::compareTo).orElse(1)).orElseGet(() -> other.contextSelectorId.isPresent() ? -1 : 0);
	}
}
