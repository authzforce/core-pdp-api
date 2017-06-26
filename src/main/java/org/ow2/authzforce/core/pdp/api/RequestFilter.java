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

import net.sf.saxon.s9api.Processor;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;

/**
 * XACML Request filter; applies some validation and processing of the input request prior to the policy evaluation. A typical request filter may support the MultiRequests element, and more generally
 * the Multiple Decision Profile by creating multiple Individual Decision Requests (EvaluationContext) from the original XACML request, as defined in XACML Multiple Decision Profile specification,
 * section 2; and then call the policy evaluation engine for each Individual Decision Request. At the end, the results (one per Individual Decision Request) may be combined by a
 * {@link DecisionResultFilter}.
 * 
 * <p>
 * This replaces and supersedes the former, now obsolete, ResourceProvider, which used to correspond to one mode of the Multiple Decision Profile for requesting multiple decisions.
 * </p>
 * 
 */
public interface RequestFilter
{

	/**
	 * Factory of RequestFilters
	 * 
	 */
	interface Factory extends PdpExtension
	{
		/**
		 * Create instance of RequestFilter
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
		 * @return instance of RequestFilter
		 */
		RequestFilter getInstance(DatatypeFactoryRegistry datatypeFactoryRegistry, boolean strictAttributeIssuerMatch, boolean requireContentForXPath, Processor xmlProcessor);
	}

	/**
	 * Filters (validates and/or transforms) a Request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile
	 * 
	 * @param req
	 *            input Request
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation; may be null if such
	 *            mapping defined
	 * 
	 * @return individual decision requests, as defined in Multiple Decision Profile, e.g. a singleton list if no multiple decision requested or supported by the filter
	 *         <p>
	 *         Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
	 *         </p>
	 * @throws IndeterminateEvaluationException
	 *             if some feature requested in the Request is not supported by this
	 */
	List<? extends IndividualXACMLRequest> filter(Request req, Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException;
}
