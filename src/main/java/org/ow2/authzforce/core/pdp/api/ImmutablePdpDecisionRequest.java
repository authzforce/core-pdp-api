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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Immutable implementation of {@link PdpDecisionRequest} to be used as input to a {@link PDPEngine}. Typically used as output request instances by PDP {@link RequestFilter} extensions, based on
 * JAXB/XACML input requests
 */
public final class ImmutablePdpDecisionRequest implements PdpDecisionRequest
{

	// initialized not null by constructors
	private final Map<AttributeGUID, Bag<?>> namedAttributes;
	private final Map<String, XdmNode> contentNodesByCategory;
	private final boolean isApplicablePolicyListReturned;

	private transient volatile int hashCode = 0; // Effective Java - Item 9

	/**
	 * Create new instance
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param contentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param returnApplicablePolicies
	 *            return list of applicable policy identifiers; equivalent of XACML Request's ReturnPolicyIdList flag
	 */
	public ImmutablePdpDecisionRequest(final Map<AttributeGUID, Bag<?>> namedAttributes, final Map<String, XdmNode> contentNodesByCategory, final boolean returnApplicablePolicies)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		this.namedAttributes = namedAttributes == null ? Collections.emptyMap() : HashCollections.newImmutableMap(namedAttributes);
		this.contentNodesByCategory = contentNodesByCategory == null ? Collections.emptyMap() : HashCollections.newImmutableMap(contentNodesByCategory);
		this.isApplicablePolicyListReturned = returnApplicablePolicies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getNamedAttributes()
	 */
	@Override
	public Map<AttributeGUID, Bag<?>> getNamedAttributes()
	{
		return this.namedAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getExtraContentsByCategory()
	 */
	@Override
	public Map<String, XdmNode> getContentNodesByCategory()
	{
		return this.contentNodesByCategory;
	}

	/**
	 * @return the returnApplicablePolicyIdList
	 */
	@Override
	public boolean isApplicablePolicyIdListReturned()
	{
		return this.isApplicablePolicyListReturned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[namedAttributes=" + namedAttributes + ", contentNodesByCategory=" + contentNodesByCategory + ", isApplicablePolicyListReturned=" + isApplicablePolicyListReturned + "]";
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
			hashCode = Objects.hash(this.namedAttributes, this.contentNodesByCategory, this.isApplicablePolicyListReturned);
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
		if (this == obj)
		{
			return true;
		}

		if (!(obj instanceof ImmutablePdpDecisionRequest))
		{
			return false;
		}

		final ImmutablePdpDecisionRequest other = (ImmutablePdpDecisionRequest) obj;
		return this.isApplicablePolicyListReturned == other.isApplicablePolicyListReturned && this.namedAttributes.equals(other.namedAttributes)
				&& this.contentNodesByCategory.equals(other.contentNodesByCategory);
	}

}
