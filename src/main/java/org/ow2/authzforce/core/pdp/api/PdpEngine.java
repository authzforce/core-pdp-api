/*
 * Copyright 2012-2021 THALES.
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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;

/**
 * This is the interface for the Authorization PDP engines, providing the starting point for decision request evaluation, independent of data representation/serialization formats.
 * 
 */
public interface PdpEngine
{
	/**
	 * Gets the PDP-engine-specific individual decision request builder.
	 * 
	 * @param expectedNumOfAttributeCategories
	 *            expected number of attribute categories in the request. This helps the implementation to allocate the right amount of memory and limit memory waste. Use negative value if unknown.
	 * @param expectedTotalNumOfAttributes
	 *            expected total number of attributes (over all categories). This helps the implementation to allocate the right amount of memory and limit memory waste. Use negative value if unknown.
	 * 
	 * @return implementation-specific request builder. May not be thread-safe.
	 */
	DecisionRequestBuilder<?> newRequestBuilder(int expectedNumOfAttributeCategories, int expectedTotalNumOfAttributes);

	/**
	 * Generic API (serialization-format-agnostic) for evaluating an individual decision request (see Multiple Decision Profile of XACML for the concept of "Individual Decision Request").
	 * <p>
	 * This method DOES NOT use any {@link org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor} or any {@link org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor}. (Only based on core
	 * PDP engine.)
	 * <p>
	 * This method does not throw any exception but may still return an Indeterminate result if an error occurred. Therefore, clients should check whether {@link DecisionResult#getDecision() ==
	 * DecisionType#INDETERMINATE}, in which case they can get more error info from {@link DecisionResult#getCauseForIndeterminate()}).
	 * 
	 * @param request
	 *            Individual Decision Request, as defined in the XACML Multiple Decision Profile (also mentioned in the Hierarchical Resource Profile)
	 * @return decision result.
	 */
	DecisionResult evaluate(DecisionRequest request);

	/**
	 * Generic API (serialization-format-agnostic) for evaluating multiple individual decision requests (see Multiple Decision Profile of XACML for the concept of "Individual Decision Request"), i.e.
	 * as part of the same context. As a result, if any attribute is set by the PDP itself, e.g. the XACML standard environment attributes (current-date/current-time/current-date-time), it MUST have
	 * the same values for all input requests.
	 * <p>
	 * This method DOES NOT use any {@link org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor} or any {@link org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor}. (Only based on core
	 * PDP engine.)
	 * <p>
	 * If the PDP uses any remote cache/database service, it should send all decision requests in the same service request and get all existing cache results in the service response, for performance
	 * reasons.
	 * 
	 * @param requests
	 *            Individual Decision Requests (see Multiple Decision Profile of XACML for the concept of "Individual Decision Request")
	 * @return decision request-result pairs
	 * @throws IndeterminateEvaluationException
	 *             error occurred preventing any request evaluation. (This error is not specific to a particular decision request. Such request-specific error results in a Indeterminate decision
	 *             result with error cause available via {@link DecisionResult#getCauseForIndeterminate()})
	 */
	<INDIVIDUAL_DECISION_REQ_T extends DecisionRequest> Collection<Entry<INDIVIDUAL_DECISION_REQ_T, ? extends DecisionResult>> evaluate(List<INDIVIDUAL_DECISION_REQ_T> requests)
			throws IndeterminateEvaluationException;

	/**
	 * Get the PDP engine's root policy and policies referenced - directly or indirectly - from the root policy, independent from the evaluation context, i.e. assuming all are statically resolved
	 *
	 * @return the root - always in first position - and referenced policies; null if any of these policies is not statically resolved (once and for all)
	 */
	Iterable<PrimaryPolicyMetadata> getApplicablePolicies();

}
