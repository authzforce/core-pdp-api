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

import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Authorization decision evaluation input, in other terms the part of the XACML request that is actually used for evaluating the policy decision. In particular, this does NOT include the
 * IncludeInResult parameter because the policy evaluation does not depend on it. The attributes with IncludeInResult=true are returned in the Result, no matter what the decision is.
 * <p>
 * One interesting use case for this class is decision caching that would consist to map a {@link DecisionInput} to a {@link DecisionResult}.
 * 
 */
public interface DecisionInput
{

	/**
	 * Get named attributes by name
	 * 
	 * @return map of attribute name-value pairs, null if none (but {@link #getExtraContentsByCategory()} result may not be empty)
	 */
	Map<AttributeGUID, Bag<?>> getNamedAttributes();

	/**
	 * Get Attributes/Contents (parsed into XDM data model for XPath evaluation) by attribute category
	 * 
	 * @return extra XML Contents by category; null if none
	 */
	Map<String, XdmNode> getExtraContentsByCategory();

	/**
	 * Get ReturnPolicyIdList flag
	 * 
	 * @return true iff original XACML Request's ReturnPolicyIdList == true
	 */
	boolean isApplicablePolicyIdListReturned();

}