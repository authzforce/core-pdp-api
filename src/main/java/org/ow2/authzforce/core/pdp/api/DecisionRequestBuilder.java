/*
 * Copyright 2012-2022 THALES.
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

import net.sf.saxon.s9api.XdmNode;

import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * Immutable {@link DecisionRequest} builder. May not be thread-safe!
 * 
 * @param <R>
 *            type of {@link DecisionRequest} that this builder builds
 */
public interface DecisionRequestBuilder<R extends DecisionRequest>
{
	/**
	 * Puts a named attribute into the request if the attribute is not already present in the request
	 * 
	 * @param attributeFQN
	 *            attribute GUID (category, ID, issuer)
	 * @param attributeValues
	 *            attribute values
	 * @return previous values for this attribute in the request, else null if there was no such attribute in the request
	 */
	Bag<?> putNamedAttributeIfAbsent(AttributeFqn attributeFQN, AttributeBag<?> attributeValues);

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
	 * Builds the final immutable {@link DecisionRequest}.
	 * 
	 * @param returnApplicablePolicies
	 *            true iff the list of applicable policy identifiers is requested; equivalent of XACML Request's ReturnPolicyIdList flag
	 * 
	 * @return a {@link DecisionRequest}
	 */
	R build(boolean returnApplicablePolicies);

	/**
	 * Reset/clear state to allow building a new request from scratch.
	 */
	void reset();

}
