/**
 * Copyright 2012-2017 Thales Services SAS.
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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;

/**
 * Attribute's Globally unique identifier, as opposed to AttributeId which is local to a specific category and/or issuer. Why not use AttributeDesignator? Because we don't care about MustBePresent or
 * Datatype for lookup here. This is used for example as key in a map to retrieve corresponding AttributeValue or AttributeProvider module.
 * <p>
 * WARNING: java.net.URI cannot be used here for XACML category and ID, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
 * </p>
 * <p>
 * [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
 * </p>
 * 
 * 
 */
public final class AttributeGUID implements Comparable<AttributeGUID>
{
	private static final IllegalArgumentException NULL_ID_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined AttributeId");
	private static final IllegalArgumentException NULL_CATEGORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined Attribute category");
	private final String category;
	private final Optional<String> issuer;
	private final String id;

	// cached method results
	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile String toString = null; // Effective Java - Item 71

	/**
	 * @return the category
	 */
	public String getCategory()
	{
		return category;
	}

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return the issuer
	 */
	public Optional<String> getIssuer()
	{
		return issuer;
	}

	/**
	 * Creates instance from XACML AttributeDesignator
	 * 
	 * @param attrDes
	 *            attribute designator
	 * @throws IllegalArgumentException
	 *             if {@code attrDes.getCategory() == null || attrDes.getAttributeId() == null}
	 */
	public AttributeGUID(final AttributeDesignatorType attrDes)
	{
		this(attrDes.getCategory(), Optional.ofNullable(attrDes.getIssuer()), attrDes.getAttributeId());
	}

	/**
	 * Creates instance from attribute category, issuer and ID
	 * 
	 * @param attrCat
	 *            attribute category (non-null)
	 * @param attrIssuer
	 *            attribute issuer (may be null)
	 * @param attrId
	 *            (non-null)
	 * @throws IllegalArgumentException
	 *             if {@code attrCat == null || attrId == null}
	 */
	public AttributeGUID(final String attrCat, final Optional<String> attrIssuer, final String attrId)
	{
		if (attrCat == null)
		{
			throw NULL_CATEGORY_ARGUMENT_EXCEPTION;
		}

		if (attrId == null)
		{
			throw NULL_ID_ARGUMENT_EXCEPTION;
		}

		category = attrCat;
		issuer = attrIssuer;
		id = attrId;
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
			hashCode = Objects.hash(category, issuer, id);
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

		if (!(obj instanceof AttributeGUID))
		{
			return false;
		}

		final AttributeGUID other = (AttributeGUID) obj;
		// category cannot be null (see constructor)
		// id cannot be null (see constructor)
		/*
		 * According to XACML Core spec, 7.3.4 Attribute Matching, if the Issuer is not supplied in the AttributeDesignator, ignore it in the match. But if the Issuer is supplied, it must match only
		 * an AttributeGUID with the same Issuer. So here we compare everything, including the Issuer, but in order to handle the first case (Issuer-less AttributeDesignator), we'll make sure that
		 * there is an Issuer-less version in the request context for each Issuer-full Attribute
		 */
		return category.equals(other.category) && id.equals(other.id) && issuer.equals(other.issuer);
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
			toString = "[category='" + category + "', issuer=" + (issuer.isPresent() ? "'" + issuer + "'" : null) + ", id='" + id + "']";
		}

		return toString;
	}

	/**
	 * Compares using lexicographical ordering on Category, then Issuer, then finally the ID (see {@link #compareTo(AttributeGUID)}.
	 */
	@Override
	public int compareTo(final AttributeGUID other)
	{
		final int thisCatComparedToOtherCat = this.category.compareTo(other.category);
		if (thisCatComparedToOtherCat != 0)
		{
			return thisCatComparedToOtherCat;
		}

		if (this.issuer.isPresent())
		{
			if (!other.issuer.isPresent())
			{

				/*
				 * this.issuer is present but other.issuer is not present -> this.issuer > other.issuer
				 */
				return 1;
			}

			// both issuers are present
			final int thisIssuerComparedToOtherIssuer = this.issuer.get().compareTo(other.issuer.get());
			if (thisIssuerComparedToOtherIssuer != 0)
			{
				return thisIssuerComparedToOtherIssuer;
			}

			// both issuers are equal -> result depends on next field
		}
		else
		{
			// this.issuer is not present
			if (other.issuer.isPresent())
			{
				/*
				 * this.issuer is not present but other.issuer is -> this.issuer < other.issuer
				 */
				return -1;
			}

			// neither issuer is present -> both issuers are equals -> result depends on next field
		}

		return this.id.compareTo(other.id);
	}
}
