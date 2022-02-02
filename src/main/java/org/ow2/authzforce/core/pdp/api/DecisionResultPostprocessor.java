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

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

/**
 * XACML Decision Result post-processor, i.e. a PDP extension that processes decision Results after policy evaluation. Each Result corresponds to an Individual Decision Request. Besides, a typical
 * Result post-processor may combine multiple individual decision results into a single decision Result if and only if the XACML Request's 'CombinedDecision' is set to true, as defined in XACML
 * Multiple Decision Profile specification, section 3.
 * <p>
 * Note: this interface is meant to be generic enough to be independent of the Result data serialization format (XML, JSON...).
 * 
 * @param <INDIVIDUAL_DECISION_REQUEST>
 *            type of individual decision request.
 * @param <OUTPUT_DECISION_RESPONSE>
 *            type of output decision response. Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
 * 
 */
public interface DecisionResultPostprocessor<INDIVIDUAL_DECISION_REQUEST extends DecisionRequest, OUTPUT_DECISION_RESPONSE>
{
	/**
	 * Standard feature identifiers that may be returned by {@link DecisionResultPostprocessor#getFeatures()}
	 *
	 */
	final class Features
	{
		private Features()
		{
			// prevent instantiation
		}

		/**
		 * Identifier of the feature described in section 3 (Requests for a combined decision) of XACML v3.0 Multiple Decision Profile.
		 */
		public static final String XACML_MULTIPLE_DECISION_PROFILE_COMBINED_DECISION = "urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision";
	}

	/**
	 * Gets the class of supported individual decision request objects
	 * 
	 * @return (individual decision) request type parameter
	 */
	Class<INDIVIDUAL_DECISION_REQUEST> getRequestType();

	/**
	 * Gets the class of output decision response
	 * 
	 * @return result type parameter
	 */
	Class<OUTPUT_DECISION_RESPONSE> getResponseType();

	/**
	 * 
	 * Get supported features, e.g. {@value Features#XACML_MULTIPLE_DECISION_PROFILE_COMBINED_DECISION} for Combined Decision
	 * 
	 * @return list of identifiers of supported features
	 */
	default Set<String> getFeatures()
	{
		return Collections.emptySet();
	}

	/**
	 * Process multiple individual decision results (e.g. combine them if CombinedDecision=true)
	 * 
	 * @param resultsByRequest
	 *            results mapped to corresponding individual decision requests for correlation
	 * @return output response
	 */
	OUTPUT_DECISION_RESPONSE process(Collection<Entry<INDIVIDUAL_DECISION_REQUEST, ? extends DecisionResult>> resultsByRequest);

	/**
	 * Process an indeterminate result, i.e. evaluation error, in case of client request error.
	 * 
	 * 
	 * @param error
	 *            client request error
	 * @return error result
	 */
	OUTPUT_DECISION_RESPONSE processClientError(final IndeterminateEvaluationException error);

	/**
	 * Process an indeterminate result, i.e. evaluation error, in case of PDP engine's internal error, as opposed to client request errors.
	 * <p>
	 * For security reasons, error details should not be included in this case as they may disclose PDP internal issues (e.g. bad configuration) to clients.
	 * 
	 * @param error
	 *            internal error
	 * @return error Result
	 */
	OUTPUT_DECISION_RESPONSE processInternalError(final IndeterminateEvaluationException error);

	/**
	 * Factory of result post-processors
	 * 
	 * @param <IDREQ>
	 *            type of original individual decision request. Some of its elements may be used in the output result, e.g. if XACML IncludeInResult="true".
	 * @param <RES>
	 *            type of output result corresponding to individual decision request(s). Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
	 */
	interface Factory<IDREQ extends DecisionRequest, RES> extends PdpExtension
	{
		/**
		 * Gets the type of individual decision requests handled by created instances
		 * 
		 * @return (individual decision) request type parameter
		 */
		Class<IDREQ> getRequestType();

		/**
		 * Gets the type of output decision response produced by created instances
		 * 
		 * @return result type parameter
		 */
		Class<RES> getResponseType();

		/**
		 * Create instance of Result postprocessor
		 * 
		 * @param clientRequestErrorVerbosityLevel
		 *            Level of verbosity of the error message trace returned in case of client request errors, e.g. invalid requests. Increasing this value usually helps the clients better pinpoint
		 *            the issue with their Requests. This parameter applies to {@link DecisionResultPostprocessor#processClientError(IndeterminateEvaluationException)} which is expected to enforce
		 *            this verbosity level when returning the error result. The Result postprocessor must return all error messages in the Java stacktrace up to the same level as this parameter's
		 *            value if the stacktrace is bigger, else the full stacktrace.
		 * @return instance of Result postprocessor
		 */
		DecisionResultPostprocessor<IDREQ, RES> getInstance(int clientRequestErrorVerbosityLevel);
	}
}
