/**
 * Copyright (C) 2012-2017 Thales Services SAS.
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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * {@link PdpDecisionRequest} builder. May not be thread-safe!
 * 
 * @param <T>
 *            type of {@link PdpDecisionRequest} that this builder builds
 */
public interface PdpDecisionRequestBuilder<T extends PdpDecisionRequest>
{
	/**
	 * Puts a named attribute into the request if the attribute is not already present in the request
	 * 
	 * @param attributeGUID
	 *            attribute GUID (category, ID, issuer)
	 * @param attributeValues
	 *            attribute values
	 * @return previous values for this attribute in the request, else null if there was no such attribute in the request
	 */
	Bag<?> putNamedAttributeIfAbsent(AttributeGUID attributeGUID, Bag<?> attributeValues);

	/**
	 * Puts extra Content (node) into a specific attribute category of the request, if the attribute category does not already have such Content in the request
	 * 
	 * @param attributeCategory
	 *            attribute category
	 * @param content
	 *            extra content
	 * @return previous content in the same attribute category, else null if there was no such content
	 */
	XdmNode putContentIfAbsent(String attributeCategory, XdmNode content);

	/**
	 * Builds the final a {@link PdpDecisionResult}
	 * 
	 * @param returnApplicablePolicies
	 *            return list of applicable policy identifiers; equivalent of XACML Request's ReturnPolicyIdList flag
	 * 
	 * @return a {@link PdpDecisionResult}
	 */
	T build(boolean returnApplicablePolicies);

	/**
	 * Reset/clear state to allow building a new request from scratch.
	 */
	void reset();

}
