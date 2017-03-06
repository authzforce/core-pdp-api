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

import java.util.Map;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Authorization decision request used as input to PDP engine in AuthzForce-specific model, in other terms the part of the XACML Request that is actually used by the PDP engine for evaluating the policy decision. In particular,
 * this does NOT include the IncludeInResult parameter because the policy evaluation does not depend on it. The attributes with IncludeInResult=true are returned in the Result, no matter what the
 * decision is.
 * <p>
 * One interesting use case for this class is decision caching that would consist to map a {@link PdpDecisionRequest} to a {@link PdpDecisionResult}.
 * 
 */
public interface PdpDecisionRequest
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