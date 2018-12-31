/**
 * Copyright 2012-2018 THALES.
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
import java.util.Set;

import net.sf.saxon.s9api.Processor;

import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;

/**
 * XACML Decision Request pre-processor; applies some validation and processing of the input request prior to the policy evaluation. For example, a request preprocessor may transform an XACML Request
 * using the Multiple Decision Profile (section 2) into multiple Individual Decision Requests; and so that the policy evaluation engine is called for each Individual Decision Request. At the end, the
 * results (one per Individual Decision Request) may be combined by a {@link DecisionResultPostprocessor}.
 * <p>
 * PDP extensions of this type (to support new ways of pre-processing XACML decision requests) must implement the {@link Factory} class
 * <p>
 * Note: this interface is meant to be generic enough to be independent of the actual original input data format (XML, JSON...).
 * 
 * @param <OUTPUT_INDIVIDUAL_DECISION_REQUEST>
 *            type of output individual decision request.
 * @param <INPUT_DECISION_REQUEST>
 *            type of original input decision request that may correspond to multiple individual decision requests (e.g. using XACML Multiple Decision Profile). Usually serializable, e.g.
 *            XACML-schema-derived JAXB Request for XML.
 */
public interface DecisionRequestPreprocessor<INPUT_DECISION_REQUEST, OUTPUT_INDIVIDUAL_DECISION_REQUEST extends DecisionRequest>
{

	/**
	 * Returns the type of input requests
	 * 
	 * @return {@code INPUT_DECISION_REQUEST} class.
	 */
	Class<INPUT_DECISION_REQUEST> getInputRequestType();

	/**
	 * Returns the type of output individual decision requests
	 * 
	 * @return {@code OUTPUT_INDIVIDUAL_DECISION_REQUEST} class.
	 */
	Class<OUTPUT_INDIVIDUAL_DECISION_REQUEST> getOutputRequestType();

	/**
	 * Pre-processes a decision request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile
	 * 
	 * @param req
	 *            input Request
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation; may be null if such
	 *            mapping defined
	 * 
	 * @return individual decision requests, as defined in Multiple Decision Profile, e.g. a singleton list if no multiple decision requested or supported by this
	 *         <p>
	 *         Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this
	 */
	List<OUTPUT_INDIVIDUAL_DECISION_REQUEST> process(INPUT_DECISION_REQUEST req, Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException;

	/**
	 * Factory of request preprocessors
	 * 
	 * @param <OIDR>
	 *            type of individual decision request.
	 * @param <IDR>
	 *            type of output result corresponding to individual decision request(s). Usually serializable, e.g. XACML-schema-derived JAXB Result for XML.
	 */
	interface Factory<IDR, OIDR extends DecisionRequest> extends PdpExtension
	{
		/**
		 * Returns the type of input requests handled by created instances
		 * 
		 * @return {@code IDR} class.
		 */
		Class<IDR> getInputRequestType();

		/**
		 * Returns the type of output individual decision requests produced by created instances
		 * 
		 * @return {@code OIDR} class.
		 */
		Class<OIDR> getOutputRequestType();

		/**
		 * Create instance of Request pre-processor
		 * 
		 * @param datatypeFactoryRegistry
		 *            attribute datatype factory for parsing XACML Request AttributeValues into Java types compatible with/optimized for the policy evaluation engine
		 * @param strictAttributeIssuerMatch
		 *            true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case
		 *            that the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full
		 *            compliance with the XACML 3.0 AttributeDesignator Evaluation: "If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute
		 *            SHALL be governed by AttributeId and DataType attributes alone."
		 * 
		 * @param requireContentForXPath
		 *            true iff XPath evaluation against Attributes/Content element is required (e.g. for AttributeSelector or xpathExpression evaluation). A preprocessor may skip Content parsing for
		 *            XPath evaluation, if and only if this is false. (Be aware that a preprocessor may support the MultipleDecision Profile or Hierarchical Profile and therefore require Content
		 *            parsing for other purposes defined by these profiles.)
		 * @param xmlProcessor
		 *            XML processor for parsing Attributes/Content prior to XPATH evaluation (e.g. AttributeSelectors). May be null if {@code requireContentForXPath} is false.
		 * @param extraPdpEngineFeatures
		 *            extra - not mandatory per XACML 3.0 core specification - features supported by the PDP engine. If a decision request requests any such non-mandatory feature (e.g.
		 *            CombinedDecision=true in XACML), the request preprocessor should use this argument to check whether it is supported by the PDP before processing the request further. See
		 *            {@link org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor.Features} for example.
		 * @return instance of Request preprocessor
		 */
		DecisionRequestPreprocessor<IDR, OIDR> getInstance(AttributeValueFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean requireContentForXPath,
				Processor xmlProcessor, Set<String> extraPdpEngineFeatures);
	}

}
