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
	private final List<Attributes> attributesToIncludeInResult;
	private final boolean returnApplicablePolicyIdList;

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

}
