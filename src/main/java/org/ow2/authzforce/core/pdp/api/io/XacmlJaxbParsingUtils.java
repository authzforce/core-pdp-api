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
package org.ow2.authzforce.core.pdp.api.io;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.DecisionResults;
import org.ow2.authzforce.core.pdp.api.HashCollections;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.PepAction;
import org.ow2.authzforce.core.pdp.api.PepActionAttributeAssignment;
import org.ow2.authzforce.core.pdp.api.XmlUtils.NoXmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.SAXBasedXmlnsFilteringParser;
import org.ow2.authzforce.core.pdp.api.XmlUtils.XmlnsFilteringParserFactory;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;
import org.ow2.authzforce.core.pdp.api.io.SingleCategoryAttributes.NamedAttributeIteratorConverter;
import org.ow2.authzforce.core.pdp.api.policy.BasePrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;
import org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.identifiers.XacmlStatusCode;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Advice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AssociatedAdvice;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignment;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Content;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligation;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Obligations;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIdentifierList;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Status;

/**
 * XACML/XML parsing utilities based on JAXB API. Mostly parse XACML/XML objects into AuthzForce data model's equivalents.
 * 
 */
public final class XacmlJaxbParsingUtils
{
	private static final IllegalArgumentException NULL_ATTRIBUTE_CATEGORY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XACML attribute category");
	private static final IllegalArgumentException NULL_INPUT_ATTRIBUTE_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined input XACML attribute arg (inputXacmlAttribute)");
	private static final IllegalArgumentException NO_JAXB_ATTRIBUTE_VALUE_LIST_ARGUMENT_EXCEPTION = new IllegalArgumentException(
	        "Input XACML attribute values null/empty (nonEmptyJaxbAttributeValues)");
	private static final XmlnsFilteringParserFactory NS_FILTERING_XACML_PARSER_FACTORY = () -> {
		final Unmarshaller unmarshaller = Xacml3JaxbHelper.createXacml3Unmarshaller();
		return new SAXBasedXmlnsFilteringParser(unmarshaller);
	};

	private static final XmlnsFilteringParserFactory NO_NS_FILTERING_XACML_PARSER_FACTORY = () -> {
		final Unmarshaller unmarshaller = Xacml3JaxbHelper.createXacml3Unmarshaller();
		return new NoXmlnsFilteringParser(unmarshaller);
	};

	/**
	 * Get XACML parser factory capable of creating namespace-filtering parsers. Such parsers can provide any namespace prefix-URI mapping used in a parsed document, and such mappings are useful for
	 * namespace-aware XPath evaluation.
	 * 
	 * @param enableFiltering
	 *            true iff a factory supporting namespace filtering is required
	 * @return XACML parser factory instance
	 */
	public static XmlnsFilteringParserFactory getXacmlParserFactory(final boolean enableFiltering)
	{
		return enableFiltering ? NS_FILTERING_XACML_PARSER_FACTORY : NO_NS_FILTERING_XACML_PARSER_FACTORY;
	}

	/**
	 * Named XACML/JAXB Attribute parser
	 */
	public static final class NamedXacmlJaxbAttributeParser extends NamedXacmlAttributeParser<Attribute>
	{
		private static <AV extends AttributeValue> NamedXacmlAttributeParsingResult<AV> parseNamedAttribute(final AttributeFqn attName, final List<AttributeValueType> nonEmptyInputXacmlAttValues,
		        final AttributeValueFactory<AV> attValFactory, final XPathCompiler xPathCompiler)
		{
			assert attName != null && nonEmptyInputXacmlAttValues != null && !nonEmptyInputXacmlAttValues.isEmpty() && attValFactory != null;

			final Collection<AV> attValues = new ArrayDeque<>(nonEmptyInputXacmlAttValues.size());
			for (final AttributeValueType inputXacmlAttValue : nonEmptyInputXacmlAttValues)
			{
				final AV resultValue = attValFactory.getInstance(inputXacmlAttValue.getContent(), inputXacmlAttValue.getOtherAttributes(), xPathCompiler);
				attValues.add(resultValue);
			}

			return new ImmutableNamedXacmlAttributeParsingResult<>(attName, attValFactory.getDatatype(), ImmutableList.copyOf(attValues));
		}

		/**
		 * Constructor
		 * 
		 * @param attributeValueFactoryRegistry
		 *            registry of datatype-specific attribute value parsers
		 */
		public NamedXacmlJaxbAttributeParser(final AttributeValueFactoryRegistry attributeValueFactoryRegistry)
		{
			super(attributeValueFactoryRegistry);
		}

		@Override
		protected NamedXacmlAttributeParsingResult<?> parseNamedAttribute(final String attributeCategoryId, final Attribute inputXacmlAttribute, final XPathCompiler xPathCompiler)
		{
			if (attributeCategoryId == null)
			{
				throw NULL_ATTRIBUTE_CATEGORY_ARGUMENT_EXCEPTION;
			}

			if (inputXacmlAttribute == null)
			{
				throw NULL_INPUT_ATTRIBUTE_ARGUMENT_EXCEPTION;
			}

			final List<AttributeValueType> inputXacmlAttValues = inputXacmlAttribute.getAttributeValues();
			if (inputXacmlAttValues == null || inputXacmlAttValues.isEmpty())
			{
				throw NO_JAXB_ATTRIBUTE_VALUE_LIST_ARGUMENT_EXCEPTION;
			}

			final AttributeFqn attName = AttributeFqns.newInstance(attributeCategoryId, Optional.ofNullable(inputXacmlAttribute.getIssuer()), inputXacmlAttribute.getAttributeId());

			/**
			 * Determine the attribute datatype to make sure it is supported and all values are of the same datatype. Indeed, XACML spec says for Attribute Bags (7.3.2): "There SHALL be no notion of a
			 * bag containing bags, or a bag containing values of differing types; i.e., a bag in XACML SHALL contain only values that are of the same data-type."
			 * <p>
			 * So we can obtain the datatypeURI/datatype class from the first value.
			 */
			final AttributeValueFactory<?> attValFactory = getAttributeValueFactory(inputXacmlAttValues.get(0).getDataType(), attName);
			return parseNamedAttribute(attName, inputXacmlAttValues, attValFactory, xPathCompiler);
		}
	}

	/**
	 * Base XACML/JAXB &lt;Attributes&gt; parser
	 * 
	 * @param <BAG>
	 *            type of bag resulting from parsing XACML AttributeValues
	 */
	private static abstract class BaseXacmlJaxbAttributesParser<BAG extends Iterable<? extends AttributeValue>> implements SingleCategoryXacmlAttributesParser<Attributes>
	{
		private final XacmlRequestAttributeParser<Attribute, BAG> xacmlReqAttributeParser;
		private final NamedAttributeIteratorConverter<BAG> namedAttrIterConverter;

		private BaseXacmlJaxbAttributesParser(final XacmlRequestAttributeParser<Attribute, BAG> xacmlRequestAttributeParser, final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
		{
			assert xacmlRequestAttributeParser != null && namedAttributeIteratorConverter != null;

			this.xacmlReqAttributeParser = xacmlRequestAttributeParser;
			this.namedAttrIterConverter = namedAttributeIteratorConverter;
		}

		/**
		 * Parse XML Content in &lt;Attributes&gt; to XPath data model for XPath evaluation
		 * 
		 * @param categoryName
		 *            category of the &lt;Attributes&gt; element
		 * @param jaxbContent
		 *            the &lt;Attributes&gt;/Content node
		 * 
		 * @return null if Content parsing not supported or disabled
		 * @throws IndeterminateEvaluationException
		 *             if any Content parsing error occurs
		 */
		protected abstract XdmNode parseContent(String categoryName, Content jaxbContent) throws IndeterminateEvaluationException;

		@Override
		public SingleCategoryAttributes<BAG, Attributes> parseAttributes(final Attributes xacmlAttributes, final XPathCompiler xPathCompiler) throws IndeterminateEvaluationException
		{
			assert xacmlAttributes != null;
			final String categoryId = xacmlAttributes.getCategory();
			/*
			 * Ignore jaxbAttrCategory.getId(), as it is primarily intended to be referenced in multiple requests when implementing MultiRequests of Multiple Decision Profile, not implemented here.
			 */
			final List<Attribute> categoryAttrs = xacmlAttributes.getAttributes();
			assert categoryAttrs != null;
			final Content jaxbAttrsContent = xacmlAttributes.getContent();
			final XdmNode extraContent = parseContent(categoryId, jaxbAttrsContent);

			final Map<AttributeFqn, BAG> attrMap;
			final Attributes attrsToIncludeInResult;
			if (categoryAttrs.isEmpty())
			{
				if (extraContent == null)
				{

					/*
					 * Skipping this <Attributes> because no <Attribute> and no extra Content parsed
					 */
					return null;
				}

				attrMap = Collections.emptyMap();
				attrsToIncludeInResult = null;
			} else
			{
				/*
				 * Let's iterate over the attributes to convert the list to a map indexed by the attribute category/id/issuer for quicker access during request evaluation. There might be multiple
				 * occurrences of <Attribute> with same meta-data (id, etc.), so the map value type need to be expandable/appendable to merge new values when new occurrences are found, e.g.
				 * Collection.
				 */
				attrMap = HashCollections.newUpdatableMap();

				/*
				 * categoryAttrs is immutable (JAXB-annotated classes have been generated as such using -immutable arg) so we cannot modify it directly to create the list of Attributes included in
				 * Result (IncludeInResult=true)
				 */
				final List<Attribute> returnedAttributes = new ArrayList<>(categoryAttrs.size());
				for (final Attribute jaxbAttr : categoryAttrs)
				{
					/*
					 * Update the attribute map with new values resulting from parsing the new XACML AttributeValues
					 */
					try
					{
						xacmlReqAttributeParser.parseNamedAttribute(categoryId, jaxbAttr, xPathCompiler, attrMap);
					} catch (final IllegalArgumentException e)
					{
						throw new IndeterminateEvaluationException("Invalid Attributes/Attribute element", XacmlStatusCode.SYNTAX_ERROR.value(), e);
					}

					// Check IncludeInResult
					if (jaxbAttr.isIncludeInResult())
					{
						returnedAttributes.add(jaxbAttr);
					}

				}

				/*
				 * If there are Attributes to include, create an <Attributes> with these but without Content to include in the Result.
				 */
				attrsToIncludeInResult = returnedAttributes.isEmpty() ? null : new Attributes(null, returnedAttributes, categoryId, xacmlAttributes.getId());
			}

			return new SingleCategoryAttributes<>(categoryId, attrMap.entrySet(), namedAttrIterConverter, attrsToIncludeInResult, extraContent);
		}
	}

	private static final class ContentSkippingXacmlJaxbAttributesParser<BAG extends Iterable<? extends AttributeValue>> extends BaseXacmlJaxbAttributesParser<BAG>
	{
		private ContentSkippingXacmlJaxbAttributesParser(final XacmlRequestAttributeParser<Attribute, BAG> xacmlRequestAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
		{
			super(xacmlRequestAttributeParser, namedAttributeIteratorConverter);
		}

		@Override
		protected XdmNode parseContent(final String categoryName, final Content jaxbContent) throws IndeterminateEvaluationException
		{
			// Content parsing not supported
			return null;
		}
	}

	private static final IllegalArgumentException NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined namedAttributeIteratorConverter");
	private static final IllegalArgumentException NULL_JAXB_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined JAXB/XACML Attribute parser (null jaxbAttributeParser)");

	/**
	 * 
	 * Factory for JAXB/XACML &lt;Attributes&gt; parser that only parses the named attributes (Attribute elements), not the Content
	 * 
	 * @param <BAG>
	 *            resulting from parsing XACML AttributeValues
	 */
	public static final class ContentSkippingXacmlJaxbAttributesParserFactory<BAG extends Iterable<? extends AttributeValue>> implements SingleCategoryXacmlAttributesParser.Factory<Attributes>
	{
		private final SingleCategoryXacmlAttributesParser<Attributes> instance;

		/**
		 * Creates instance
		 * 
		 * @param xacmlReqAttributeParser
		 *            parser used to parse each JAXB/XACML &lt;Attribute&gt;
		 * @param namedAttributeIteratorConverter
		 *            converts iterator over attributes with values produced by {@code jaxbAttributeParser}, into constant-valued/immutable attribute iterator
		 * @throws IllegalArgumentException
		 *             {@code if(jaxbAttributeParser == null || namedAttributeIteratorConverter == null)}
		 */
		public ContentSkippingXacmlJaxbAttributesParserFactory(final XacmlRequestAttributeParser<Attribute, BAG> xacmlReqAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter) throws IllegalArgumentException
		{
			instance = new ContentSkippingXacmlJaxbAttributesParser<>(xacmlReqAttributeParser, namedAttributeIteratorConverter);
		}

		@Override
		public SingleCategoryXacmlAttributesParser<Attributes> getInstance()
		{
			return instance;
		}

	}

	private static final class FullXacmlJaxbAttributesParser<BAG extends Iterable<? extends AttributeValue>> extends BaseXacmlJaxbAttributesParser<BAG>
	{
		// XML document builder for parsing Content to XPath data model for XPath evaluation
		private final DocumentBuilder xmlDocBuilder;

		private FullXacmlJaxbAttributesParser(final XacmlRequestAttributeParser<Attribute, BAG> xacmlReqAttributeParser, final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter,
		        final DocumentBuilder xmlDocBuilder)
		{
			super(xacmlReqAttributeParser, namedAttributeIteratorConverter);
			assert xmlDocBuilder != null;
			this.xmlDocBuilder = xmlDocBuilder;
		}

		@Override
		public XdmNode parseContent(final String categoryName, final Content jaxbContent) throws IndeterminateEvaluationException
		{
			if (jaxbContent == null)
			{
				// nothing to parse
				return null;
			}

			// XACML spec, 7.3.7: the document node must be the single child element of Content.
			Element childElt = null;
			for (final Serializable node : jaxbContent.getContent())
			{
				if (node instanceof Element)
				{
					childElt = (Element) node;
					break;
				}
			}

			if (childElt == null)
			{
				throw new IndeterminateEvaluationException("Invalid Content of Attributes[@Category=" + categoryName + "] for XPath evaluation: no child element",
				        XacmlStatusCode.SYNTAX_ERROR.value());
			}

			try
			{
				return xmlDocBuilder.wrap(childElt);
			} catch (final IllegalArgumentException e)
			{
				throw new IndeterminateEvaluationException("Error parsing Content of Attributes[@Category=" + categoryName + "] for XPath evaluation", XacmlStatusCode.SYNTAX_ERROR.value(), e);
			}

		}

	}

	/**
	 * 
	 * Factory for JAXB/XACML &lt;Attributes&gt; Parser that parses the named attributes (Attribute elements), and the free-form Content
	 * 
	 * @param <BAG>
	 *            resulting from parsing XACML AttributeValues
	 */
	public static final class FullXacmlJaxbAttributesParserFactory<BAG extends Iterable<? extends AttributeValue>> implements SingleCategoryXacmlAttributesParser.Factory<Attributes>
	{
		private static final IllegalArgumentException NULL_XML_PROCESSOR_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined XML processor (null xmlProcessor)");
		private final XacmlRequestAttributeParser<Attribute, BAG> xacmlReqAttributeParser;
		private final NamedAttributeIteratorConverter<BAG> namedAttrIterConverter;
		private final Processor xmlProc;

		/**
		 * Creates instance
		 * 
		 * @param xacmlReqAttributeParser
		 *            parser used to parse each JAXB/XACML &lt;Attribute&gt;
		 * @param namedAttributeIteratorConverter
		 *            converts iterator over attributes with values produced by {@code jaxbAttributeParser}, into constant-valued/immutable attribute iterator
		 * @param xmlProcessor
		 *            SAXON XML processor to process the Attributes/Content node
		 * @throws IllegalArgumentException
		 *             {@code if(jaxbAttributeParser == null || namedAttributeIteratorConverter == null || xmlProcessor == null)}
		 */
		public FullXacmlJaxbAttributesParserFactory(final XacmlRequestAttributeParser<Attribute, BAG> xacmlReqAttributeParser,
		        final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter, final Processor xmlProcessor)
		{
			if (xacmlReqAttributeParser == null)
			{
				throw NULL_JAXB_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION;
			}

			if (namedAttributeIteratorConverter == null)
			{
				throw NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION;
			}

			if (xmlProcessor == null)
			{
				throw NULL_XML_PROCESSOR_ARGUMENT_EXCEPTION;
			}

			this.xacmlReqAttributeParser = xacmlReqAttributeParser;
			this.namedAttrIterConverter = namedAttributeIteratorConverter;
			this.xmlProc = xmlProcessor;
		}

		@Override
		public SingleCategoryXacmlAttributesParser<Attributes> getInstance()
		{
			// create instance of inner class (has access to this.xmlProc)
			return new FullXacmlJaxbAttributesParser<>(xacmlReqAttributeParser, namedAttrIterConverter, xmlProc.newDocumentBuilder());
		}
	}

	private static <AV extends AttributeValue> PepActionAttributeAssignment<AV> newPepActionAttributeAssignment(String attributeId, Optional<String> category, Optional<String> issuer,
	        ConstantExpression<AV> constantExp)
	{
		return new PepActionAttributeAssignment<>(attributeId, category, issuer, constantExp.getReturnType(), constantExp.getValue().get());
	}

	private static ImmutableList<PepActionAttributeAssignment<?>> xacmlToAuthzForceAttributeAssignments(List<AttributeAssignment> xacmlAttributeAssignments,
	        AttributeValueFactoryRegistry attributeValueFactories)
	{
		final List<PepActionAttributeAssignment<?>> attAssignments = new ArrayList<>(xacmlAttributeAssignments.size());
		for (final AttributeAssignment xacmlAttAssig : xacmlAttributeAssignments)
		{
			final ConstantExpression<? extends AttributeValue> constantExp = attributeValueFactories.newExpression(xacmlAttAssig.getDataType(), xacmlAttAssig.getContent(),
			        xacmlAttAssig.getOtherAttributes(), null);
			final PepActionAttributeAssignment<?> attAssignment = newPepActionAttributeAssignment(xacmlAttAssig.getAttributeId(), Optional.ofNullable(xacmlAttAssig.getCategory()),
			        Optional.ofNullable(xacmlAttAssig.getIssuer()), constantExp);
			attAssignments.add(attAssignment);
		}

		return ImmutableList.copyOf(attAssignments);
	}

	/**
	 * Parse/convert XACML/XML Result into AuthzForce decision result
	 * 
	 * @param xacmlResult
	 *            XACML/XML Result (XML-schema-derived JAXB model)
	 * @param attributeValueFactories
	 *            AttributeValue factories (registry of datatype-specific parsers)
	 * @return decision result in AuthzForce data model
	 */
	public static DecisionResult parseXacmlJaxbResult(final Result xacmlResult, AttributeValueFactoryRegistry attributeValueFactories)
	{
		final PolicyIdentifierList xacmlPolicyIdentifiers = xacmlResult.getPolicyIdentifierList();
		final ImmutableList<PrimaryPolicyMetadata> immutableApplicablePolicyIdList;
		if (xacmlPolicyIdentifiers == null)
		{
			immutableApplicablePolicyIdList = null;
		} else
		{
			final List<PrimaryPolicyMetadata> applicablePolicyIdentifiers = xacmlPolicyIdentifiers.getPolicyIdReferencesAndPolicySetIdReferences().stream().map(jaxbElt -> {
				final IdReferenceType idRef = jaxbElt.getValue();
				return new BasePrimaryPolicyMetadata(jaxbElt.getName().getLocalPart().equals("PolicyIdReference") ? TopLevelPolicyElementType.POLICY : TopLevelPolicyElementType.POLICY_SET,
				        idRef.getValue(), new PolicyVersion(idRef.getVersion()));
			}).collect(Collectors.toList());

			immutableApplicablePolicyIdList = ImmutableList.copyOf(applicablePolicyIdentifiers);
		}

		final Obligations xacmlObligations = xacmlResult.getObligations();
		final List<Obligation> nonNullXacmlObligationList;
		if (xacmlObligations == null)
		{
			nonNullXacmlObligationList = Collections.emptyList();
		} else
		{
			final List<Obligation> xacmlObligationList = xacmlObligations.getObligations();
			nonNullXacmlObligationList = xacmlObligationList == null ? Collections.emptyList() : xacmlObligationList;
		}

		final AssociatedAdvice xacmlAdvice = xacmlResult.getAssociatedAdvice();
		final List<Advice> nonNullXacmlAdviceList;
		if (xacmlAdvice == null)
		{
			nonNullXacmlAdviceList = Collections.emptyList();
		} else
		{
			final List<Advice> xacmlAdviceList = xacmlAdvice.getAdvices();
			nonNullXacmlAdviceList = xacmlAdviceList == null ? Collections.emptyList() : xacmlAdviceList;
		}

		final ImmutableList<PepAction> pepActions;
		if (nonNullXacmlObligationList.isEmpty() && nonNullXacmlAdviceList.isEmpty())
		{
			pepActions = ImmutableList.of();
		} else
		{
			final List<PepAction> mutablePepActions = new ArrayList<>(nonNullXacmlObligationList.size() + nonNullXacmlAdviceList.size());
			nonNullXacmlObligationList.forEach(xacmlOb -> {
				mutablePepActions.add(new PepAction(xacmlOb.getObligationId(), true, xacmlToAuthzForceAttributeAssignments(xacmlOb.getAttributeAssignments(), attributeValueFactories)));
			});

			nonNullXacmlAdviceList.forEach(xacmlAd -> {
				mutablePepActions.add(new PepAction(xacmlAd.getAdviceId(), false, xacmlToAuthzForceAttributeAssignments(xacmlAd.getAttributeAssignments(), attributeValueFactories)));
			});

			pepActions = ImmutableList.copyOf(mutablePepActions);
		}

		final Status status = xacmlResult.getStatus();
		switch (xacmlResult.getDecision())
		{
			case DENY:
				return DecisionResults.getDeny(status, pepActions, immutableApplicablePolicyIdList);
			case PERMIT:
				return DecisionResults.getPermit(status, pepActions, immutableApplicablePolicyIdList);
			case NOT_APPLICABLE:
				return DecisionResults.getNotApplicable(status);
			default:
				return DecisionResults.newIndeterminate(null, new IndeterminateEvaluationException(status.getStatusMessage(), status.getStatusCode().getValue()), immutableApplicablePolicyIdList);
		}
	}

	private XacmlJaxbParsingUtils()
	{
	}

	/*
	 * Testing XACML parsing
	 */
	// public static void main(String[] args) throws JAXBException, SAXException, ParserConfigurationException,
	// IOException
	// {

	// SAXParserFactory spf = SAXParserFactory.newInstance();
	// spf.setNamespaceAware(true);
	// XMLReader xmlReader = spf.newSAXParser().getXMLReader();
	// XMLFilter xmlFilter = new XMLFilterImpl(xmlReader)
	// {
	//
	// @Override
	// public void startPrefixMapping(String prefix, String uri) throws SAXException
	// {
	// System.out.println(prefix + " -> " + uri);
	// super.startPrefixMapping(prefix, uri);
	// }
	//
	// };
	//
	// Unmarshaller unmarshaller = createXacml3Unmarshaller();
	// UnmarshallerHandler unmarshallHandler = unmarshaller.getUnmarshallerHandler();
	// xmlFilter.setContentHandler(unmarshallHandler);
	// xmlFilter.parse(new
	// InputSource("src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIA024/IIA024Request.xml"));
	// Request request = (Request) unmarshallHandler.getResult();
	// // Request request = (Request) unmarshaller.unmarshal(new InputSource(
	// // "src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIA024/IIA024Request.xml"));
	// System.out.println(request);
	// System.out.println("############################################################");
	// xmlFilter.parse(new
	// InputSource("src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIA001/IIA001Policy.xml"));
	// Policy policy = (Policy) unmarshallHandler.getResult();
	// // Policy policy = (Policy) unmarshaller.unmarshal(new InputSource(
	// // "src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIA001/IIA001Policy.xml"));
	// System.out.println(policy);
	// System.out.println("############################################################");
	// xmlFilter.parse(new
	// InputSource("src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIB300/IIB300Policy.xml"));
	// PolicySet policySet = (PolicySet) unmarshallHandler.getResult();
	// // PolicySet policySet = (PolicySet) unmarshaller.unmarshal(new InputSource(
	// // "src/test/resources/conformance/xacml-3.0-from-2.0-ct/mandatory/IIB300/IIB300Policy.xml"));
	// System.out.println(policySet);
	//
	// }
}
