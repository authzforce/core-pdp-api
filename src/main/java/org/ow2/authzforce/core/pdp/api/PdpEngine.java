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

import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

/**
 * This is the interface for the XACML PDP engines, providing the starting point for request evaluation.
 * 
 * @param <INDIVIDUAL_DECISION_REQ_T>
 *            PDP implementation-specific type of individual decision request
 */
public interface PdpEngine<INDIVIDUAL_DECISION_REQ_T extends PdpDecisionRequest>
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
	PdpDecisionRequestBuilder<INDIVIDUAL_DECISION_REQ_T> newRequestBuilder(int expectedNumOfAttributeCategories, int expectedTotalNumOfAttributes);

	/**
	 * Generic API (serialization-format-agnostic) for evaluating an individual decision request according to XACML specification. To be used instead of {@link #evaluate(Request)} or
	 * {@link #evaluate(Request, Map)} when calling the PDP Java API directly (native Java call, e.g. embedded PDP), or when the original request format is NOT XML.
	 * <p>
	 * This method DOES NOT use any {@link org.ow2.authzforce.core.pdp.api.RequestFilter} or any {@link org.ow2.authzforce.core.pdp.api.DecisionResultFilter}. (Only based on core PDP engine.)
	 * 
	 * @param request
	 *            Individual Decision Request, as defined in the XACML Multiple Decision Profile (also mentioned in the Hierarchical Resource Profile)
	 * @return decision result
	 */
	PdpDecisionResult evaluate(INDIVIDUAL_DECISION_REQ_T request);

	/**
	 * Generic API (serialization-format-agnostic) for evaluating multiple individual decision requests in the same way as defined in XACML Multiple Decision Profile, i.e. as part of the same context.
	 * As a result, if any attribute is set by the PDP itself, e.g. the XACML standard environment attributes (current-date/current-time/current-date-time), it MUST have the same values for all input
	 * requests.
	 * <p>
	 * To be used instead of {@link #evaluate(Request)} or {@link #evaluate(Request, Map)} when calling the PDP Java API directly (native Java call, e.g. embedded PDP), or when the original request
	 * format is NOT XML.
	 * <p>
	 * This method DOES NOT use any {@link org.ow2.authzforce.core.pdp.api.RequestFilter} or any {@link org.ow2.authzforce.core.pdp.api.DecisionResultFilter}. (Only based on core PDP engine.)
	 * <p>
	 * If the PDP uses any remote cache service, it should send all decision requests in the same service request and get all existing cache results in the service response, for performance reasons.
	 * 
	 * @param requests
	 *            Individual Decision Requests, as defined in the XACML Multiple Decision Profile (also mentioned in the Hierarchical Resource Profile)
	 * @return decision request-result pairs
	 * @throws IndeterminateEvaluationException
	 *             error occurred preventing any request evaluation (not request-specific)
	 */
	Map<INDIVIDUAL_DECISION_REQ_T, ? extends PdpDecisionResult> evaluate(List<INDIVIDUAL_DECISION_REQ_T> requests) throws IndeterminateEvaluationException;

	/**
	 * Evaluates a XML/JAXB-based XACML decision request
	 * <p>
	 * Note that if the request is somehow invalid (it was missing a required attribute, it was using an unsupported scope, etc), then the result will be a decision of INDETERMINATE.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation
	 * @return the response to the request
	 */
	Response evaluate(Request request, Map<String, String> namespaceURIsByPrefix);

	/**
	 * Equivalent to {@link #evaluate(Request, Map)} with second parameter set to null.
	 * 
	 * @param request
	 *            the request to evaluate
	 * @return the response to the request
	 */
	Response evaluate(Request request);

}
