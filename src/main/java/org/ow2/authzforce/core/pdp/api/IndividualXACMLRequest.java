/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;

import org.ow2.authzforce.core.pdp.api.value.Bag;

import com.google.common.collect.ImmutableList;

/**
 * (Immutable) Individual XACML decision request, as defined by Multiple Decision Profile of XACML. This differs from {@link PdpDecisionRequest} only by the fact that the XACML request may require in
 * addition, esp. in a Multiple Decision, that a sequence of Attributes elements from the request be included in the XACML Result as well, in order for the requester to correlate with the Attributes
 * elements in the request, i.e. the individual requests.
 *
 */
public final class IndividualXACMLRequest implements PdpDecisionRequest
{
	private final ImmutablePdpDecisionRequest baseRequest;
	private final List<Attributes> attributesToBeReturned;

	/**
	 * Creates instance from an XACML-agnostic request
	 * 
	 * @param baseRequest
	 *            base request in XACML-agnostic model
	 * @param attributesToBeReturned
	 *            attributes to be included in corresponding XACML Result
	 */
	public IndividualXACMLRequest(final ImmutablePdpDecisionRequest baseRequest, final ImmutableList<Attributes> attributesToBeReturned)
	{
		assert baseRequest != null;

		this.baseRequest = baseRequest;
		this.attributesToBeReturned = attributesToBeReturned == null ? Collections.emptyList() : attributesToBeReturned;
	}

	@Override
	public Map<AttributeGUID, Bag<?>> getNamedAttributes()
	{
		return baseRequest.getNamedAttributes();
	}

	@Override
	public Map<String, XdmNode> getContentNodesByCategory()
	{
		return baseRequest.getContentNodesByCategory();
	}

	@Override
	public boolean isApplicablePolicyIdListReturned()
	{
		return baseRequest.isApplicablePolicyIdListReturned();
	}

	/**
	 * Attributes elements to be included in corresponding result.
	 * 
	 * @return Attributes elements to be included in the decision result
	 */
	public List<Attributes> getAttributesToBeReturned()
	{
		return this.attributesToBeReturned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		/*
		 * attributesToBeReturned ignored for the PdpDecisionRequest fields to be only ones used for matching keys in DecisionCaches
		 */
		return baseRequest.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		/*
		 * attributesToBeReturned ignored for the PdpDecisionRequest fields to be only ones used for matching keys in DecisionCaches
		 */
		return super.equals(obj);
	}

}
