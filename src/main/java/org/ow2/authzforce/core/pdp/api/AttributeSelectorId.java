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

import java.util.Objects;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeSelectorType;

/**
 * AttributeSelector identifier (category, contextSelectorId, path). Why not use AttributeSelector directly? Because we
 * don't care about MustBePresent or Datatype for lookup here. This is used for example as key in a map to retrieve
 * corresponding AttributeValue when it has already been evaluated.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ContextSelectorId, because not equivalent to XML
 * schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * 
 */
public final class AttributeSelectorId
{
	private static final IllegalArgumentException NULL_PATH_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined AttributeSelector Path");
	private static final IllegalArgumentException NULL_CATEGORY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined AttributeSelector Category");
	private final String category;
	private final String path;
	private final String contextSelectorId;

	// cached method results
	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * Creates instance from XACML AttributeSelector
	 * 
	 * @param attrSelector
	 *            attribute selector
	 */
	public AttributeSelectorId(final AttributeSelectorType attrSelector)
	{

		category = attrSelector.getCategory();
		path = attrSelector.getPath();
		if (category == null)
		{
			throw NULL_CATEGORY_ARGUMENT_EXCEPTION;
		}

		if (path == null)
		{
			throw NULL_PATH_ARGUMENT_EXCEPTION;
		}

		contextSelectorId = attrSelector.getContextSelectorId();
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
	public String getContextSelectorId()
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

		if (!(obj instanceof AttributeSelectorId))
		{
			return false;
		}

		final AttributeSelectorId other = (AttributeSelectorId) obj;
		if (!this.category.equals(other.category) || !this.path.equals(other.path))
		{
			return false;
		}
		return this.contextSelectorId == null ? other.contextSelectorId == null
				: this.contextSelectorId.equals(other.contextSelectorId);
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
}
