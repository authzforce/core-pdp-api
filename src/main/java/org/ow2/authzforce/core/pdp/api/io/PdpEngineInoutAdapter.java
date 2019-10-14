/**
 * Copyright 2012-2019 THALES.
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
package org.ow2.authzforce.core.pdp.api.io;

import java.io.Closeable;
import java.util.Map;

import org.ow2.authzforce.core.pdp.api.PdpEngine;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;

/**
 * PDP engine adapter that basically adapts/wraps a {@link PdpEngine} to support extra types of input/output decision request/response (e.g. XACML/JAXB (XML) or XACML JSON Profile).
 *
 * 
 * @param <INPUT_DECISION_REQUEST>
 *            type of original input decision request handled by this class. It may correspond to multiple individual decision requests (e.g. using XACML Multiple Decision Profile). Usually
 *            serializable, e.g. XACML-schema-derived JAXB Request for XML.
 * @param <OUTPUT_DECISION_RESULT>
 *            type of output result corresponding to ADAPTER_INPUT_DECISION_REQUEST. Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
 *
 */
public interface PdpEngineInoutAdapter<INPUT_DECISION_REQUEST, OUTPUT_DECISION_RESULT> extends Closeable
{

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
	OUTPUT_DECISION_RESULT evaluate(final INPUT_DECISION_REQUEST request, final Map<String, String> namespaceURIsByPrefix);

	/**
	 * Same as {@link #evaluate(Object, Map)} but with null/empty {@code namespaceURIsByPrefix}
	 * 
	 * @param request
	 *            the request to evaluate
	 * @return the response to the request
	 */
	OUTPUT_DECISION_RESULT evaluate(final INPUT_DECISION_REQUEST request);

	/**
	 * Get the PDP engine's evaluated root policy and policies referenced - directly or indirectly - from the root policy, independent from the evaluation context, i.e. assuming all are statically
	 * resolved
	 *
	 * @return the root - always in first position - and referenced policies; null if any of these policies is not statically resolved (once and for all)
	 */
	Iterable<PrimaryPolicyMetadata> getApplicablePolicies();

}