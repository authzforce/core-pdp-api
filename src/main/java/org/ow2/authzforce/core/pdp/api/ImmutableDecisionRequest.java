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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import net.sf.saxon.s9api.XdmNode;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable implementation of {@link DecisionRequest} to be used as input to {@link PdpEngine#evaluate(DecisionRequest)}. Typically, it is used as output request instances by PDP {@link DecisionRequestPreprocessor}
 * extensions, based on JAXB/XACML input requests
 */
public final class ImmutableDecisionRequest implements DecisionRequest
{

	// initialized not null by constructors
	private final Instant creationTimestamp;
	private final ImmutableMap<AttributeFqn, AttributeBag<?>> namedAttributes;
	private final ImmutableMap<String, XdmNode> extraContentByCategory;
	private final boolean isApplicablePolicyListReturned;

	private transient volatile int hashCode = 0; // Effective Java - Item 9
	private transient volatile String toString = null;

	private ImmutableDecisionRequest(final ImmutableMap<AttributeFqn, AttributeBag<?>> immutableNamedAttributes, final ImmutableMap<String, XdmNode> immutableContentNodesByCategory,
									 final boolean returnApplicablePolicies)
	{
		assert immutableNamedAttributes != null && immutableContentNodesByCategory != null;

		this.creationTimestamp = Instant.now();
		this.namedAttributes = immutableNamedAttributes;
		this.extraContentByCategory = immutableContentNodesByCategory;
		this.isApplicablePolicyListReturned = returnApplicablePolicies;
	}

	/**
	 * Create new instance returning unsorted map of named attributes and content nodes by attribute category
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param contentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param returnApplicablePolicies
	 *            return list of applicable policy identifiers; equivalent of XACML Request's ReturnPolicyIdList flag
	 * @return new instance
	 */
	public static ImmutableDecisionRequest getInstance(final Map<AttributeFqn, AttributeBag<?>> namedAttributes, final Map<String, XdmNode> contentNodesByCategory,
			final boolean returnApplicablePolicies)
	{
		return new ImmutableDecisionRequest(namedAttributes == null ? ImmutableMap.of() : ImmutableMap.copyOf(namedAttributes),
				contentNodesByCategory == null ? ImmutableMap.of() : ImmutableMap.copyOf(contentNodesByCategory), returnApplicablePolicies);
	}

	/**
	 * Create new instance returning named attributes sorted by attribute name ( {@link #getNamedAttributes()}), and content nodes sorted by attribute category name (
	 * {@link #getExtraContentsByCategory()})
	 * 
	 * @param namedAttributes
	 *            named Attributes (no extra Content element)
	 * @param contentNodesByCategory
	 *            extra XML Content elements by attribute Category
	 * @param returnApplicablePolicies
	 *            return list of applicable policy identifiers; equivalent of XACML Request's ReturnPolicyIdList flag
	 * @return new instance
	 */
	public static ImmutableDecisionRequest getSortedInstance(final Map<AttributeFqn, AttributeBag<?>> namedAttributes, final Map<String, XdmNode> contentNodesByCategory,
			final boolean returnApplicablePolicies)
	{
		return new ImmutableDecisionRequest(namedAttributes == null ? ImmutableSortedMap.of() : ImmutableSortedMap.copyOf(namedAttributes), contentNodesByCategory == null ? ImmutableSortedMap.of()
				: ImmutableSortedMap.copyOf(contentNodesByCategory), returnApplicablePolicies);
	}

	@Override
	public Instant getCreationTimestamp()
	{
		return this.creationTimestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getNamedAttributes()
	 */
	@Override
	public ImmutableMap<AttributeFqn, AttributeBag<?>> getNamedAttributes()
	{
		return this.namedAttributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.IndividualDecisionRequest#getExtraContentsByCategory()
	 */
	@Override
	public ImmutableMap<String, XdmNode> getExtraContentsByCategory()
	{
		return this.extraContentByCategory;
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
		if (toString == null)
		{
			toString = "[namedAttributes=" + namedAttributes + ", contentNodesByCategory=" + extraContentByCategory + ", isApplicablePolicyListReturned=" + isApplicablePolicyListReturned + "]";
		}

		return toString;
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
			hashCode = Objects.hash(this.namedAttributes, this.extraContentByCategory, this.isApplicablePolicyListReturned);
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

		if (!(obj instanceof ImmutableDecisionRequest))
		{
			return false;
		}

		final ImmutableDecisionRequest other = (ImmutableDecisionRequest) obj;
		return this.isApplicablePolicyListReturned == other.isApplicablePolicyListReturned && this.namedAttributes.equals(other.namedAttributes)
				&& this.extraContentByCategory.equals(other.extraContentByCategory);
	}

}
