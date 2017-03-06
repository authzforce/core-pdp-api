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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Mutable Individual Decision Request
 */
public final class ImmutableIndividualDecisionRequest implements IndividualDecisionRequest
{
	private final Map<AttributeGUID, Bag<?>> attributes;
	private final Map<String, XdmNode> extraContentsByCategory;
	private final boolean returnApplicablePolicyIdList;
	private final List<Attributes> attributesToIncludeInResult;

	/**
	 * Create new instance
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param extraContentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param includedInResult
	 *            attributes to be include in the final Result
	 * @param returnPolicyIdList
	 *            XACML Request's ReturnPolicyIdList flag
	 */
	public ImmutableIndividualDecisionRequest(final Map<AttributeGUID, Bag<?>> namedAttributes, final Map<String, XdmNode> extraContentNodesByCategory, final List<Attributes> includedInResult,
			final boolean returnPolicyIdList)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		attributes = namedAttributes == null ? null : HashCollections.newImmutableMap(namedAttributes);
		extraContentsByCategory = extraContentNodesByCategory == null ? null : HashCollections.newImmutableMap(extraContentNodesByCategory);
		attributesToIncludeInResult = includedInResult == null ? null : Collections.unmodifiableList(includedInResult);
		returnApplicablePolicyIdList = returnPolicyIdList;
	}

	/**
	 * Create new instance as a clone of an existing request.
	 * 
	 * @param baseRequest
	 *            replicated existing request. Further changes to it are not reflected back to this new instance.
	 */
	public ImmutableIndividualDecisionRequest(final IndividualDecisionRequest baseRequest)
	{
		// these maps/lists may be updated later by put(...) method defined in this class
		this(baseRequest.getNamedAttributes(), baseRequest.getExtraContentsByCategory(), baseRequest.getReturnedAttributes(), baseRequest.isApplicablePolicyIdListReturned());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getNamedAttributes()
	 */
	@Override
	public Map<AttributeGUID, Bag<?>> getNamedAttributes()
	{
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getAttributesIncludedInResult()
	 */
	@Override
	public List<Attributes> getReturnedAttributes()
	{
		return this.attributesToIncludeInResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getExtraContentsByCategory()
	 */
	@Override
	public Map<String, XdmNode> getExtraContentsByCategory()
	{
		return this.extraContentsByCategory;
	}

	/**
	 * @return the returnApplicablePolicyIdList
	 */
	@Override
	public boolean isApplicablePolicyIdListReturned()
	{
		return returnApplicablePolicyIdList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[namedAttributes=" + attributes + ", extraContentsByCategory=" + extraContentsByCategory + ", attributesToIncludeInResult=" + attributesToIncludeInResult
				+ ", returnApplicablePolicyIdList=" + returnApplicablePolicyIdList + "]";
	}

}
