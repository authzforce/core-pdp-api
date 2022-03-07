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
package org.ow2.authzforce.core.pdp.api.io;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaults;
import org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor;
import org.ow2.authzforce.core.pdp.api.DecisionResultPostprocessor;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.MutableAttributeBag;
import org.ow2.authzforce.core.pdp.api.expression.BasicImmutableXPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils.ContentSkippingXacmlJaxbAttributesParserFactory;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils.FullXacmlJaxbAttributesParserFactory;
import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils.NamedXacmlJaxbAttributeParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.xacml.identifiers.XPathVersion;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Convenient base class for {@link DecisionRequestPreprocessor} implementations supporting core XACML-schema-defined XML input handled by JAXB framework
 */
public abstract class BaseXacmlJaxbRequestPreprocessor implements DecisionRequestPreprocessor<Request, IndividualXacmlJaxbRequest>
{

    private static final UnsupportedOperationException UNSUPPORTED_MODE_EXCEPTION = new UnsupportedOperationException(
            "Unsupported BaseXacmlJaxbRequestPreprocessor mode: allowAttributeDuplicates == false && strictAttributeIssuerMatch == false");
    /**
     * Indeterminate exception to be thrown iff CombinedDecision element is not supported
     */
    private static final IndeterminateEvaluationException UNSUPPORTED_COMBINED_DECISION_EXCEPTION = new IndeterminateEvaluationException("Unsupported CombinedDecision value in Request: 'true'",
            XacmlStatusCode.SYNTAX_ERROR.value());

    /**
     * Indeterminate exception to be thrown iff MultiRequests element not supported by the request preprocessor
     */
    protected static final IndeterminateEvaluationException UNSUPPORTED_MULTI_REQUESTS_EXCEPTION = new IndeterminateEvaluationException("Unsupported element in Request: <MultiRequests>",
            XacmlStatusCode.SYNTAX_ERROR.value());
    private static final IllegalArgumentException NULL_REQUEST_EXCEPTION = new IllegalArgumentException("Undefined input decision request");

    private final SingleCategoryXacmlAttributesParser.Factory<Attributes> xacmlAttrsParserFactory;
    private final boolean isCombinedDecisionSupported;

    /**
     * Creates instance of request pre-processor.
     *
     * @param attributeValueFactoryRegistry registry of datatype-specific attribute value parsers
     * @param strictAttributeIssuerMatch    true iff it is required that AttributeDesignator without Issuer only match request Attributes without Issuer. This mode is not fully compliant with XACML 3.0, §5.29, in the case that
     *                                      the Issuer is not present; but it performs better and is recommended when all AttributeDesignators have an Issuer (best practice). Set it to false, if you want full compliance with
     *                                      the XACML 3.0 Attribute Evaluation: "If the Issuer is not present in the attribute designator, then the matching of the attribute to the named attribute SHALL be governed by
     *                                      AttributeId and DataType attributes alone."
     * @param allowAttributeDuplicates      true iff the pre-processor should allow defining multivalued attributes by repeating the same XACML Attribute (same AttributeId) within a XACML Attributes element (same Category).
     *                                      Indeed, not allowing this is not fully compliant with the XACML spec according to a discussion on the xacml-dev mailing list (see
     *                                      {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), referring to the XACML 3.0 core spec, §7.3.3, that indicates that multiple occurrences of the
     *                                      same &lt;Attribute&gt; with same meta-data but different values should be considered equivalent to a single &lt;Attribute&gt; element with same meta-data and merged values
     *                                      (multivalued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior: the multiple subject-id Attributes are expected to result in a multi-value bag
     *                                      during evaluation of the &lt;AttributeDesignator&gt;.
     *                                      <p>
     *                                      Setting this parameter to {@code false} is not fully compliant, but provides better performance, especially if you know the Requests to be well-formed, i.e. all AttributeValues of a
     *                                      given Attribute are grouped together in the same &lt;Attribute&gt; element. Combined with {@code strictAttributeIssuerMatch == true}, this is the most efficient alternative (although
     *                                      not fully compliant).
     * @param requireContentForXPath        true iff Attributes/Content parsing (into XDM) for XPath evaluation is required
     * @param extraPdpFeatures              extra - non-mandatory per XACML 3.0 core specification - features supported by PDP engine. Any feature requested by any request is checked against this before processing the request
     *                                      further. If some feature is not supported, an Indeterminate Result is returned.
     * @throws UnsupportedOperationException if {@code strictAttributeIssuerMatch == false && allowAttributeDuplicates == false} which is not supported
     */
    protected BaseXacmlJaxbRequestPreprocessor(final AttributeValueFactoryRegistry attributeValueFactoryRegistry, final boolean strictAttributeIssuerMatch, final boolean allowAttributeDuplicates,
                                               final boolean requireContentForXPath, final Set<String> extraPdpFeatures) throws UnsupportedOperationException
    {

        final NamedXacmlAttributeParser<Attribute> namedXacmlAttParser = new NamedXacmlJaxbAttributeParser(attributeValueFactoryRegistry);
        if (allowAttributeDuplicates)
        {
            final XacmlRequestAttributeParser<Attribute, MutableAttributeBag<?>> xacmlAttributeParser = strictAttributeIssuerMatch
                    ? new NonIssuedLikeIssuedLaxXacmlAttributeParser<>(namedXacmlAttParser)
                    : new IssuedToNonIssuedCopyingLaxXacmlAttributeParser<>(namedXacmlAttParser);
            this.xacmlAttrsParserFactory = requireContentForXPath
                    ? new FullXacmlJaxbAttributesParserFactory<>(xacmlAttributeParser, SingleCategoryAttributes.MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER)
                    : new ContentSkippingXacmlJaxbAttributesParserFactory<>(xacmlAttributeParser, SingleCategoryAttributes.MUTABLE_TO_CONSTANT_ATTRIBUTE_ITERATOR_CONVERTER);
        } else // allowAttributeDuplicates == false
            if (strictAttributeIssuerMatch)
            {
                final XacmlRequestAttributeParser<Attribute, AttributeBag<?>> xacmlAttributeParser = new NonIssuedLikeIssuedStrictXacmlAttributeParser<>(namedXacmlAttParser);
                this.xacmlAttrsParserFactory = requireContentForXPath
                        ? new FullXacmlJaxbAttributesParserFactory<>(xacmlAttributeParser, SingleCategoryAttributes.IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER)
                        : new ContentSkippingXacmlJaxbAttributesParserFactory<>(xacmlAttributeParser, SingleCategoryAttributes.IDENTITY_ATTRIBUTE_ITERATOR_CONVERTER);
            } else
            {
                /*
                 * allowAttributeDuplicates == false && strictAttributeIssuerMatch == false is not supported, because it would require using mutable bags for "Issuer-less" attributes (updated for each
                 * possible Attribute with same meta-data except a defined Issuer), whereas the goal of 'allowAttributeDuplicates == false' is to use immutable Bags in the first place, i.e. to avoid going
                 * through mutable bags. A solution would consist in creating two collections of attributes, one with immutable bags, and the other with mutable ones for Issuer-less attributes. However, we
                 * consider it is not worth providing an implementation for this natively, so far. Can always been a custom RequestPreprocessor provided as an extension.
                 */
                throw UNSUPPORTED_MODE_EXCEPTION;
            }

        this.isCombinedDecisionSupported = extraPdpFeatures.contains(DecisionResultPostprocessor.Features.XACML_MULTIPLE_DECISION_PROFILE_COMBINED_DECISION);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor#getInputRequestType()
     */
    @Override
    public final Class<Request> getInputRequestType()
    {
        return Request.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor#getOutputRequestType()
     */
    @Override
    public final Class<IndividualXacmlJaxbRequest> getOutputRequestType()
    {
        return IndividualXacmlJaxbRequest.class;
    }

    /**
     * Pre-processes (validates and/or transforms) a Request, may result in multiple individual decision requests, e.g. if implementing the Multiple Decision Profile or Hierarchical Resource profile
     *
     * @param attributesList                   list of XACML Request Attributes elements
     * @param xacmlAttrsParser                 XACML Attributes element Parser instance, used to parse each Attributes in {@code attributesList}.
     * @param isApplicablePolicyIdListReturned XACML Request's property {@code returnPolicyIdList}.
     * @param combinedDecision                 XACML Request's property {@code isCombinedDecision}
     * @param xPathCompiler                    xpathExpression compiler, corresponding to the XACML RequestDefaults element, or null if no RequestDefaults element or XPath support disabled globally by PDP configuration.
     * @param namespaceURIsByPrefix            namespace prefix-URI mappings (e.g. "... xmlns:prefix=uri") in the original XACML Request bound to {@code req}, used as part of the context for XPath evaluation. If {@code xPathCompiler.isPresent()}, {@code xPathCompiler.get().getDeclaredNamespacePrefixToUriMap()} provides the mappings instead and namespaceURIsByPrefix shall be empty
     * @return individual decision requests, as defined in Multiple Decision Profile, e.g. a singleton list if no multiple decision requested or supported by the pre-processor
     * <p>
     * Return a Collection and not array to make it easy for the implementer to create a defensive copy with Collections#unmodifiableList() and alike.
     * </p>
     * @throws IndeterminateEvaluationException if some feature requested in the Request is not supported by this pre-processor
     */
    public abstract List<IndividualXacmlJaxbRequest> process(List<Attributes> attributesList, SingleCategoryXacmlAttributesParser<Attributes> xacmlAttrsParser,
                                                             boolean isApplicablePolicyIdListReturned, boolean combinedDecision, Optional<XPathCompilerProxy> xPathCompiler, Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException;

    @Override
    public final List<IndividualXacmlJaxbRequest> process(final Request jaxbRequest, final Map<String, String> namespaceURIsByPrefix) throws IndeterminateEvaluationException
    {
        if (jaxbRequest == null)
        {
            throw NULL_REQUEST_EXCEPTION;
        }

        /*
         * No support for MultiRequests (§2.4 of Multiple Decision Profile).
         */
        if (jaxbRequest.getMultiRequests() != null)
        {
            /*
             * According to 7.19.1 Unsupported functionality, return Indeterminate with syntax-error code for unsupported element
             */
            throw UNSUPPORTED_MULTI_REQUESTS_EXCEPTION;
        }

        /*
         * No support for CombinedDecision = true if no decisionCombiner defined. (The use of the CombinedDecision attribute is specified in Multiple Decision Profile.)
         */
        if (jaxbRequest.isCombinedDecision() && !this.isCombinedDecisionSupported)
        {
            /*
             * According to XACML core spec, 5.42, <i>If the PDP does not implement the relevant functionality in [Multiple Decision Profile], then the PDP must return an Indeterminate with a status
             * code of urn:oasis:names:tc:xacml:1.0:status:processing-error if it receives a request with this attribute set to "true"</i>.
             */
            throw UNSUPPORTED_COMBINED_DECISION_EXCEPTION;
        }

        final RequestDefaults jaxbReqDefaults = jaxbRequest.getRequestDefaults();
        final Optional<XPathCompilerProxy> xPathCompiler;
        final Map<String, String> newNsPrefixToUriMap;
        if (jaxbReqDefaults == null)
        {
            xPathCompiler = Optional.empty();
            newNsPrefixToUriMap = namespaceURIsByPrefix;
        } else
        {
            try
            {
                final XPathVersion xPathVersion = XPathVersion.fromURI(jaxbReqDefaults.getXPathVersion());
                xPathCompiler = Optional.of(new BasicImmutableXPathCompilerProxy(xPathVersion, namespaceURIsByPrefix));
				/*
				namespaceURIsByPrefix already held by xPathCompiler and retrievable from it with getDeclaredNamespacePrefixToUriMap().
				 */
                newNsPrefixToUriMap = Map.of();
            } catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("Invalid/unsupported XPathVersion in Request/RequestDefaults", e);
            }
        }

        final SingleCategoryXacmlAttributesParser<Attributes> xacmlAttrsParser = xacmlAttrsParserFactory.getInstance();
        return process(jaxbRequest.getAttributes(), xacmlAttrsParser, jaxbRequest.isReturnPolicyIdList(), jaxbRequest.isCombinedDecision(), xPathCompiler, newNsPrefixToUriMap);
    }

    /**
     * Convenient base class for {@link org.ow2.authzforce.core.pdp.api.DecisionRequestPreprocessor.Factory} implementations supporting core XACML-schema-defined XML input handled by JAXB framework
     */
    public static abstract class Factory implements DecisionRequestPreprocessor.Factory<Request, IndividualXacmlJaxbRequest>
    {
        private final String id;

        protected Factory(final String id)
        {
            this.id = id;
        }

        @Override
        public final String getId()
        {
            return id;
        }

        @Override
        public final Class<Request> getInputRequestType()
        {
            return Request.class;
        }

        @Override
        public final Class<IndividualXacmlJaxbRequest> getOutputRequestType()
        {
            return IndividualXacmlJaxbRequest.class;
        }
    }

}
