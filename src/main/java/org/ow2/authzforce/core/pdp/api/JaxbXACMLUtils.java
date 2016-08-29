/**
 * Copyright (C) 2012-2016 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api;

import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.ow2.authzforce.core.pdp.api.SingleCategoryAttributes.NamedAttributeIteratorConverter;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.api.XMLUtils.NoNamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.api.XMLUtils.SAXBasedNamespaceFilteringParser;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactoryRegistry;
import org.ow2.authzforce.xacml.identifiers.XACMLAttributeId;
import org.ow2.authzforce.xacml.identifiers.XACMLCategory;
import org.ow2.authzforce.xacml.identifiers.XACMLResourceScope;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.koloboke.collect.map.hash.HashObjObjMaps;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmNode;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Attributes;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Content;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;

/**
 * XACML processing utilities based on JAXB API
 * 
 */
public final class JaxbXACMLUtils
{
	private final static String[] CLASSPATH_RELATIVE_XACML_3_0_SCHEMA_LOCATIONS = { "xml.xsd",
			"xacml-core-v3-schema-wd-17.xsd" };

	/**
	 * Name of Java package of XACML-3.0-schema-derived classes
	 */
	private static final String XACML_3_0_JAXB_PACKAGE_NAME = "oasis.names.tc.xacml._3_0.core.schema.wd_17";

	/**
	 * XACML 3.0 schema
	 */
	private static final Schema XACML_3_0_SCHEMA;
	static
	{
		final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final Source[] schemaSources = new Source[2];
		final ClassLoader classLoader = JaxbXACMLUtils.class.getClassLoader();
		for (int i = 0; i < CLASSPATH_RELATIVE_XACML_3_0_SCHEMA_LOCATIONS.length; i++)
		{
			final URL schemaURL = classLoader.getResource(CLASSPATH_RELATIVE_XACML_3_0_SCHEMA_LOCATIONS[i]);
			if (schemaURL == null)
			{
				throw new RuntimeException("Invalid XML schema location on classpath (not found): "
						+ CLASSPATH_RELATIVE_XACML_3_0_SCHEMA_LOCATIONS[i]);
			}

			schemaSources[i] = new StreamSource(schemaURL.toExternalForm());
		}

		try
		{
			XACML_3_0_SCHEMA = factory.newSchema(schemaSources);
		}
		catch (final SAXException e)
		{
			throw new RuntimeException("Failed to load XML schemas on classpath: "
					+ Arrays.toString(CLASSPATH_RELATIVE_XACML_3_0_SCHEMA_LOCATIONS), e);
		}
	}

	/**
	 * JAXB context for (un)marshalling from/to JAXB objects derived from XACML 3.0 schema
	 */
	public static final JAXBContext XACML_3_0_JAXB_CONTEXT;
	static
	{
		try
		{
			XACML_3_0_JAXB_CONTEXT = JAXBContext.newInstance(XACML_3_0_JAXB_PACKAGE_NAME,
					JaxbXACMLUtils.class.getClassLoader());
		}
		catch (final JAXBException e)
		{
			throw new RuntimeException("Error instantiating JAXB context for (un)marshalling from/to XACML 3.0 objects",
					e);
		}
	}

	/**
	 * XACML 3.0 JAXB ObjectFactory
	 */
	public static final ObjectFactory XACML_3_0_OBJECT_FACTORY = new ObjectFactory();

	/**
	 * Creates XACML 3.0/JAXB unmarshaller with XACML 3.0 schema validation
	 * 
	 * @return unmarshaller
	 * @throws JAXBException
	 *             if an error was encountered while creating the Unmarshaller object
	 */
	public static Unmarshaller createXacml3Unmarshaller() throws JAXBException
	{
		final Unmarshaller unmarshaller = XACML_3_0_JAXB_CONTEXT.createUnmarshaller();
		unmarshaller.setSchema(XACML_3_0_SCHEMA);
		return unmarshaller;
	}

	/**
	 * Creates XACML 3.0/JAXB marshaller with XACML 3.0 schema validation
	 * 
	 * @return marshaller
	 * @throws JAXBException
	 *             if an error was encountered while creating the Marshaller object
	 */
	public static Marshaller createXacml3Marshaller() throws JAXBException
	{
		final Marshaller marshaller = XACML_3_0_JAXB_CONTEXT.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
		marshaller.setSchema(XACML_3_0_SCHEMA);
		return marshaller;
	}

	private static final IllegalArgumentException NULL_DATATYPE_FACTORY_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Cannot instantiate XACML attribute parser: input datatypeFactoryRegistry undefined");
	private static final IllegalArgumentException NO_JAXB_ATTRIBUTE_VALUE_LIST_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Input XACML attribute values null/empty (nonEmptyJaxbAttributeValues)");
	private static final IllegalArgumentException NULL_ATTRIBUTE_GUID_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined input ID of attribute to be parsed (null attributeGUID)");
	private static final IllegalArgumentException NULL_OUT_ATTRIBUTE_MAP_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Null input attributeMap (required) for attributes resulting from parsing");

	/**
	 * (Namespace-filtering) XACML-to-JAXB parser factory
	 *
	 */
	public interface XACMLParserFactory
	{
		/**
		 * Get factory instance
		 * 
		 * @return instance
		 * @throws JAXBException
		 *             if any error instantiating XACML-to-JAXB parser
		 */
		NamespaceFilteringParser getInstance() throws JAXBException;
	}

	private static final XACMLParserFactory NS_FILTERING_XACML_PARSER_FACTORY = new XACMLParserFactory()
	{
		@Override
		public NamespaceFilteringParser getInstance() throws JAXBException
		{
			final Unmarshaller unmarshaller = createXacml3Unmarshaller();
			return new SAXBasedNamespaceFilteringParser(unmarshaller);
		}
	};

	private static final XACMLParserFactory NO_NS_FILTERING_XACML_PARSER_FACTORY = new XACMLParserFactory()
	{

		@Override
		public NamespaceFilteringParser getInstance() throws JAXBException
		{
			final Unmarshaller unmarshaller = createXacml3Unmarshaller();
			return new NoNamespaceFilteringParser(unmarshaller);
		}

	};

	/**
	 * Get XACML parser factory capable of creating namespace-filtering parsers. Such parsers can provide any namespace
	 * prefix-URI mapping used in a parsed document, and such mappings are useful for namespace-aware XPath evaluation.
	 * 
	 * @param enableFiltering
	 *            true iff a factory supporting namespace filtering is required
	 * @return XACML parser factory instance
	 */
	public static XACMLParserFactory getXACMLParserFactory(final boolean enableFiltering)
	{
		return enableFiltering ? NS_FILTERING_XACML_PARSER_FACTORY : NO_NS_FILTERING_XACML_PARSER_FACTORY;
	}

	/**
	 * 
	 * XACML/JAXB &lt;Attribute&gt; parsing helper
	 *
	 */
	private static final class JaxbXACMLAttributeParsingHelper
	{
		private static final AttributeGUID RESOURCE_SCOPE_ATTRIBUTE_GUID = new AttributeGUID(
				XACMLCategory.XACML_3_0_RESOURCE_CATEGORY_RESOURCE.value(), null,
				XACMLAttributeId.XACML_RESOURCE_SCOPE.value());

		private static final IllegalArgumentException UNSUPPORTED_MULTIPLE_SCOPE_EXCEPTION = new IllegalArgumentException(
				"Unsupported resource scope. Expected scope: none or " + XACMLResourceScope.IMMEDIATE.value()
						+ ". (Profile 'urn:oasis:names:tc:xacml:3.0:profile:multiple:scope' not supported.)");

		private final DatatypeFactoryRegistry datatypeFactoryRegistry;

		private JaxbXACMLAttributeParsingHelper(final DatatypeFactoryRegistry datatypeFactoryRegistry)
		{
			assert datatypeFactoryRegistry != null;
			this.datatypeFactoryRegistry = datatypeFactoryRegistry;
		}

		protected DatatypeFactory<?> getDatatypeFactory(final AttributeValueType jaxbAttributeValue)
				throws IllegalArgumentException
		{
			assert jaxbAttributeValue != null;

			final String datatypeURI = jaxbAttributeValue.getDataType();
			final DatatypeFactory<?> datatypeFactory = datatypeFactoryRegistry.getExtension(datatypeURI);
			if (datatypeFactory == null)
			{
				throw new IllegalArgumentException("Invalid AttributeValue DataType: " + datatypeURI);
			}

			return datatypeFactory;
		}

		private static void validateResourceScope(final AttributeGUID attributeGUID,
				final AttributeValueType jaxbAttrVal0) throws IllegalArgumentException
		{
			assert attributeGUID != null && jaxbAttrVal0 != null;
			/*
			 * Check whether this is not an unsupported resource-scope attribute. XACML Multiple Decision Profile, §
			 * 2.3.3: "... If such a <Attributes> element contains a 'scope' attribute having any value other than
			 * 'Immediate', then the Individual Request SHALL be further processed according to the processing model
			 * specified in Section 4." We do not support 'scope' other than 'Immediate' so throw an error if different.
			 */
			if (attributeGUID.equals(RESOURCE_SCOPE_ATTRIBUTE_GUID))
			{
				final List<Serializable> jaxbContent = jaxbAttrVal0.getContent();
				if (!jaxbContent.isEmpty() && !jaxbContent.get(0).equals(XACMLResourceScope.IMMEDIATE.value()))
				{
					throw UNSUPPORTED_MULTIPLE_SCOPE_EXCEPTION;
				}
			}
		}
	}

	/**
	 * JAXB/XACML &lt;Attribute&gt; parser
	 *
	 * @param <BAG>
	 *            type of attribute value bag resulting from parsing the AttributeValues
	 */
	public interface JaxbXACMLAttributeParser<BAG extends Iterable<? extends AttributeValue>>
	{
		/**
		 * "Strict" parsing method, that parse all the values of a given attribute in one call. In short, this method
		 * will reject multiple calls on the same Attribute identifier (same metadata).
		 * 
		 * @param attributeMap
		 *            request attribute map to be updated by the result of parsing {@code nonEmptyJaxbAttributeValues}
		 * @param attributeGUID
		 *            attribute unique identifier
		 * @param nonEmptyJaxbAttributeValues
		 *            (non-empty list of JAXB/XACML AttributeValues)
		 * @param xPathCompiler
		 *            XPath compiler for compiling/evaluating XPath expressions in values, such as XACML
		 *            xpathExpressions
		 * @throws IllegalArgumentException
		 *             if parsing of the {@code nonEmptyJaxbAttributeValues} because of invalid datatype or mixing of
		 *             different datatypes; or if there are already existing values for {@code attributeGUID} in
		 *             {@code attributeMap}
		 */
		void parseAttribute(Map<AttributeGUID, BAG> attributeMap, AttributeGUID attributeGUID,
				List<AttributeValueType> nonEmptyJaxbAttributeValues, XPathCompiler xPathCompiler)
				throws IllegalArgumentException;
	}

	/**
	 * On the contrary to {@link IssuedToNonIssuedCopyingLaxJaxbXACMLAttributeParser}, this JAXB/XACML Attribute parser
	 * does not copy the values of Attributes having an Issuer to the corresponding Attributes without Issuer (same
	 * Category, AttributeId...) in the resulting attribute map. Therefore it does not comply with what XACML 3.0, §5.29
	 * says on &lt;AttributeDesignator&gt; evaluation. However, it is more performant. In this implementation, an
	 * Attribute with no Issuer is handled like an attribute with an Issuer, except the Issuer has the special value
	 * "null". Therefore, an AttributeDesignator with "null" Issuer (undefined) will still match any attribute in the
	 * request with "null" Issuer (but not any other Attribute with same AttributeId but a defined/non-null Issuer, for
	 * which a different AttributeDesignator with a defined Issuer must be used).
	 * <p>
	 * "Strict" means it does not allow defining multi-valued attributes by repeating the same XACML Attribute (same
	 * AttributeId) within a XACML Attributes element (same Category). This is not fully compliant with the XACML spec
	 * according to a discussion on the xacml-dev mailing list (see
	 * {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), referring to the XACML 3.0
	 * core spec, §7.3.3, that indicates that multiple occurrences of the same &lt;Attribute&gt; with same meta-data but
	 * different values should be considered equivalent to a single &lt;Attribute&gt; element with same meta-data and
	 * merged values (multi-valued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior:
	 * the multiple subject-id Attributes are expected to result in a multi-value bag during evaluation of the
	 * &lt;AttributeDesignator&gt;.
	 * <p>
	 * In a nutshell, this type of attribute parser does not comply fully with XACML 3.0. However, to benefit fully from
	 * the XACML capabilities, it is strongly recommended to avoid such Attribute repetitions and group all the values
	 * of the same Attribute in the same Attribute element with multiple AttributeValues. In that case, you will achieve
	 * better performances by using this "strict" parser instead of the "lax" version.
	 *
	 */
	public static final class NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser
			implements JaxbXACMLAttributeParser<Bag<?>>
	{
		private final JaxbXACMLAttributeParsingHelper helper;

		/**
		 * Creates instance of XACML/JAXB Attribute Parser using a given registry of datatype factories
		 * 
		 * @param datatypeFactoryRegistry
		 *            registry of datatype factories for parsing Attribute values into native Java model
		 * @throws IllegalArgumentException
		 *             iff {@code datatypeFactoryRegistry == null}
		 */
		public NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser(final DatatypeFactoryRegistry datatypeFactoryRegistry)
				throws IllegalArgumentException
		{
			if (datatypeFactoryRegistry == null)
			{
				throw NULL_DATATYPE_FACTORY_REGISTRY_ARGUMENT_EXCEPTION;
			}

			this.helper = new JaxbXACMLAttributeParsingHelper(datatypeFactoryRegistry);
		}

		private static <AV extends AttributeValue> Bag<AV> parseValues(
				final List<AttributeValueType> nonEmptyJaxbAttributeValues, final DatatypeFactory<AV> datatypeFactory,
				final XPathCompiler xPathCompiler)
		{
			assert nonEmptyJaxbAttributeValues != null && datatypeFactory != null;

			final Collection<AV> vals = new ArrayDeque<>(nonEmptyJaxbAttributeValues.size());
			for (final AttributeValueType jaxbAttrValue : nonEmptyJaxbAttributeValues)
			{
				final AV resultValue = datatypeFactory.getInstance(jaxbAttrValue.getContent(),
						jaxbAttrValue.getOtherAttributes(), xPathCompiler);
				vals.add(resultValue);
			}

			return Bags.getInstance(datatypeFactory.getDatatype(), vals);
		}

		@Override
		public void parseAttribute(final Map<AttributeGUID, Bag<?>> attributeMap, final AttributeGUID attributeGUID,
				final List<AttributeValueType> nonEmptyJaxbAttributeValues, final XPathCompiler xPathCompiler)
				throws IllegalArgumentException
		{
			if (attributeMap == null)
			{
				throw NULL_OUT_ATTRIBUTE_MAP_ARGUMENT_EXCEPTION;
			}

			if (attributeGUID == null)
			{
				throw NULL_ATTRIBUTE_GUID_ARGUMENT_EXCEPTION;
			}

			if (nonEmptyJaxbAttributeValues == null || nonEmptyJaxbAttributeValues.isEmpty())
			{
				throw NO_JAXB_ATTRIBUTE_VALUE_LIST_ARGUMENT_EXCEPTION;
			}

			/*
			 * Check if it is a resource-scope.
			 */
			final AttributeValueType jaxbAttrVal0 = nonEmptyJaxbAttributeValues.get(0);
			JaxbXACMLAttributeParsingHelper.validateResourceScope(attributeGUID, jaxbAttrVal0);

			/**
			 * Determine the attribute datatype to make sure it is supported and all values are of the same datatype.
			 * Indeed, XACML spec says for Attribute Bags (7.3.2): "There SHALL be no notion of a bag containing bags,
			 * or a bag containing values of differing types; i.e., a bag in XACML SHALL contain only values that are of
			 * the same data-type."
			 * <p>
			 * So we can obtain the datatypeURI/datatype class from the first value.
			 */
			final DatatypeFactory<?> datatypeFactory;
			try
			{
				datatypeFactory = this.helper.getDatatypeFactory(jaxbAttrVal0);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid AttributeValue DataType in Attribute" + attributeGUID, e);
			}

			final Bag<?> newAttrVals;
			try
			{
				newAttrVals = parseValues(nonEmptyJaxbAttributeValues, datatypeFactory, xPathCompiler);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid AttributeValue(s) in Attribute" + attributeGUID, e);
			}

			/*
			 * If there is any existing values for the same attrGUID (<Attribute> with same meta-data) in the map, it
			 * will be rejected. This behavior is not fully compliant with XACML (see the Javadoc of this class),
			 * however it is faster than the compliant alternative.
			 */
			final Bag<?> oldVals = attributeMap.put(attributeGUID, newAttrVals);
			if (oldVals != null)
			{
				throw new IllegalArgumentException(
						"Unsupported syntax: duplicate <Attribute> with metadata: " + attributeGUID);
			}

			/*
			 * In this implementation, we do not comply fully with XACML 3.0, §5.29, since we handle Attribute(s)
			 * without Issuer exactly like the ones with an Issuer. In other words, an undefined issuer is handled like
			 * the special "null" Issuer. Therefore, an AttributeDesignators without Issuer will not match the request
			 * attributes with matching Category, AttributeId... but a defined therefore different Issuer. It will only
			 * match the request attribute without Issuer. In a compliant implementation, we would check if the
			 * attribute has an Issuer, and if it does, also update the attribute variant with same meta-data except no
			 * Issuer.
			 */
		}

	}

	/**
	 * 
	 * "Lax" XACML/JAXB Attribute parser. "Lax" means it allows defining multi-valued attributes by repeating the same
	 * XACML Attribute (same AttributeId) within a XACML Attributes element (same Category) but with possibly different
	 * AttributeValues. As discussed on the xacml-dev mailing list (see
	 * {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), the XACML 3.0 core spec,
	 * §7.3.3, indicates that multiple occurrences of the same &lt;Attribute&gt; with same meta-data but different
	 * values should be considered equivalent to a single &lt;Attribute&gt; element with same meta-data and merged
	 * values (multi-valued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior: the
	 * multiple subject-id Attributes are expected to result in a multi-value bag during evaluation of the
	 * &lt;AttributeDesignator&gt;.
	 * <p>
	 * It is strongly recommended for better performance to avoid such Attribute repetitions and group all the values of
	 * the same Attribute in the same Attribute element with multiple AttributeValues, using the "strict" parser
	 * ({@link NonIssuedLikeIssuedStrictJaxbXACMLAttributeParser} instead of the "lax" variant.
	 *
	 */
	private static abstract class LaxJaxbXACMLAttributeParser implements JaxbXACMLAttributeParser<MutableBag<?>>
	{
		private final JaxbXACMLAttributeParsingHelper helper;

		/**
		 * Creates instance of XACML/JAXB Attribute Parser using a given registry of datatype factories
		 * 
		 * @param datatypeFactoryRegistry
		 *            registry of datatype factories for parsing Attribute values into native Java model
		 * @throws IllegalArgumentException
		 *             iff {@code datatypeFactoryRegistry == null}
		 */
		private LaxJaxbXACMLAttributeParser(final DatatypeFactoryRegistry datatypeFactoryRegistry)
				throws IllegalArgumentException
		{
			if (datatypeFactoryRegistry == null)
			{
				throw NULL_DATATYPE_FACTORY_REGISTRY_ARGUMENT_EXCEPTION;
			}

			this.helper = new JaxbXACMLAttributeParsingHelper(datatypeFactoryRegistry);
		}

		/**
		 * Decide whether to copy values of attributes with Issuer to attributes with same category and ID but null
		 * Issuer
		 * 
		 * @return
		 */
		protected abstract boolean copyIssuedAttributeValuesToNonIssued(AttributeGUID attributeGUID);

		@Override
		public void parseAttribute(final Map<AttributeGUID, MutableBag<?>> attributeMap,
				final AttributeGUID attributeGUID, final List<AttributeValueType> nonEmptyJaxbAttributeValues,
				final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			if (attributeMap == null)
			{
				throw NULL_OUT_ATTRIBUTE_MAP_ARGUMENT_EXCEPTION;
			}

			if (attributeGUID == null)
			{
				throw NULL_ATTRIBUTE_GUID_ARGUMENT_EXCEPTION;
			}

			if (nonEmptyJaxbAttributeValues == null || nonEmptyJaxbAttributeValues.isEmpty())
			{
				throw NO_JAXB_ATTRIBUTE_VALUE_LIST_ARGUMENT_EXCEPTION;
			}

			/**
			 * Determine the attribute datatype to make sure it is supported and all values are of the same datatype.
			 * Indeed, XACML spec says for Attribute Bags (7.3.2): "There SHALL be no notion of a bag containing bags,
			 * or a bag containing values of differing types; i.e., a bag in XACML SHALL contain only values that are of
			 * the same data-type."
			 * <p>
			 * So we can obtain the datatypeURI/datatype class from the first value.
			 */
			final AttributeValueType jaxbAttrVal0 = nonEmptyJaxbAttributeValues.get(0);
			final DatatypeFactory<?> datatypeFactory;
			try
			{
				datatypeFactory = helper.getDatatypeFactory(jaxbAttrVal0);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Invalid AttributeValue DataType in Attribute" + attributeGUID, e);
			}

			/*
			 * Input AttributeValues have now been validated. Let's check any existing values for the same attrGUID
			 * (<Attribute> with same meta-data) in the map. As discussed on the xacml-dev mailing list (see
			 * https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html), the XACML 3.0 core spec, §7.3.3,
			 * indicates that multiple occurrences of the same <Attribute> with same meta-data but different values
			 * should be considered equivalent to a single <Attribute> element with same meta-data and merged values
			 * (multi-valued Attribute). Moreover, the conformance test 'IIIA024' expects this behavior: the multiple
			 * subject-id Attributes are expected to result in a multi-value bag during evaluation of the
			 * AttributeDesignator.
			 * 
			 * Therefore, we choose to merge the attribute values here if this is a new occurrence of the same
			 * Attribute, i.e. attrMap.get(attrGUID) != null. In this case, we can reuse the list already created for
			 * the previous occurrence to store the new values resulting from parsing.
			 */
			final MutableBag<?> previousAttrVals = attributeMap.get(attributeGUID);
			final MutableBag<?> newAttrVals;
			if (previousAttrVals == null)
			{
				/*
				 * First occurrence of this attribute ID (attrGUID). Check whether this is not an unsupported
				 * resource-scope attribute.
				 */
				JaxbXACMLAttributeParsingHelper.validateResourceScope(attributeGUID, jaxbAttrVal0);
				newAttrVals = new MutableBag<>(datatypeFactory, xPathCompiler);
				attributeMap.put(attributeGUID, newAttrVals);
			}
			else
			{
				/*
				 * Collection of values already in the map for this Attribute id, reuse/update it directly
				 */
				newAttrVals = previousAttrVals;
			}

			/*
			 * XACML 3.0, §5.29 says on <AttributeDesignator>: "If the Issuer is not present in the attribute
			 * designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId
			 * and DataType attributes
			 * alone." Therefore, if this attribute has an Issuer, we copy its values to the "Issuer-
			 * less" version for evaluating later any matching "Issuer-less" Attribute Designator.
			 */
			int jaxbValIndex = 0;
			try
			{
				if (copyIssuedAttributeValuesToNonIssued(attributeGUID))
				{
					final MutableBag<?> newIssuerLessAttrVals;
					// attribute has an Issuer -> prepare to update the matching Issuer-less attribute values
					final AttributeGUID issuerLessId = new AttributeGUID(attributeGUID.getCategory(), null,
							attributeGUID.getId());
					final MutableBag<?> oldIssuerLessAttrVals = attributeMap.get(issuerLessId);
					if (oldIssuerLessAttrVals == null)
					{
						newIssuerLessAttrVals = new MutableBag<>(datatypeFactory, xPathCompiler);
						attributeMap.put(issuerLessId, newIssuerLessAttrVals);
					}
					else
					{
						newIssuerLessAttrVals = oldIssuerLessAttrVals;
					}

					for (final AttributeValueType jaxbAttrValue : nonEmptyJaxbAttributeValues)
					{
						newIssuerLessAttrVals.add(newAttrVals.addFromJAXB(jaxbAttrValue));
						jaxbValIndex++;
					}
				}
				else
				{
					for (final AttributeValueType jaxbAttrValue : nonEmptyJaxbAttributeValues)
					{
						newAttrVals.addFromJAXB(jaxbAttrValue);
						jaxbValIndex++;
					}
				}
			}
			catch (final IllegalArgumentException e)
			{
				throw new IllegalArgumentException(
						"Invalid AttributeValue #" + jaxbValIndex + " for Attribute" + attributeGUID, e);
			}
		}
	}

	/**
	 * 
	 * This XACML/JAXB Attribute parser copies the values of Attributes having an Issuer to the corresponding Attributes
	 * without Issuer (same Category, AttributeId...) in the result attribute map. This is a way to comply with XACML
	 * 3.0, §5.29 that says on &lt;AttributeDesignator&gt; evaluation: "If the Issuer is not present in the attribute
	 * designator, then the matching of the attribute to the named attribute SHALL be governed by AttributeId and
	 * DataType attributes alone."
	 */
	public static final class IssuedToNonIssuedCopyingLaxJaxbXACMLAttributeParser extends LaxJaxbXACMLAttributeParser
	{

		/**
		 * Creates instance of this XACML/JAXB Attribute Parser using a given registry of datatype factories
		 * 
		 * @param datatypeFactoryRegistry
		 *            registry of datatype factories for parsing Attribute values into native Java model
		 * @throws IllegalArgumentException
		 *             if {@code datatypeFactoryRegistry == null}
		 */
		public IssuedToNonIssuedCopyingLaxJaxbXACMLAttributeParser(
				final DatatypeFactoryRegistry datatypeFactoryRegistry) throws IllegalArgumentException
		{
			super(datatypeFactoryRegistry);
		}

		@Override
		protected boolean copyIssuedAttributeValuesToNonIssued(final AttributeGUID attributeGUID)
		{
			return attributeGUID.getIssuer() != null;
		}

	}

	/**
	 * On the contrary to {@link IssuedToNonIssuedCopyingLaxJaxbXACMLAttributeParser}, this JAXB/XACML Attribute parser
	 * does not copy the values of Attributes having an Issuer to the corresponding Attributes without Issuer (same
	 * Category, AttributeId...) in the resulting attribute map. Therefore it does not comply with what XACML 3.0, §5.29
	 * says on &lt;AttributeDesignator&gt; evaluation. However, it is more performant. In this implementation, an
	 * Attribute with no Issuer is handled like an attribute with an Issuer, except the Issuer has the special value
	 * "null". Therefore, an AttributeDesignator with "null" Issuer (undefined) will still match any attribute in the
	 * request with "null" Issuer (but not any other Attribute with same AttributeId but a defined/non-null Issuer, for
	 * which a different AttributeDesignator with a defined Issuer must be used).
	 *
	 */
	public static final class NonIssuedLikeIssuedLaxJaxbXACMLAttributeParser extends LaxJaxbXACMLAttributeParser
	{
		/**
		 * Creates instance of this XACML/JAXB Attribute Parser using a given registry of datatype factories
		 * 
		 * @param datatypeFactoryRegistry
		 *            registry of datatype factories for parsing Attribute values into native Java model
		 * @throws IllegalArgumentException
		 *             if {@code datatypeFactoryRegistry == null}
		 */
		public NonIssuedLikeIssuedLaxJaxbXACMLAttributeParser(final DatatypeFactoryRegistry datatypeFactoryRegistry)
				throws IllegalArgumentException
		{
			super(datatypeFactoryRegistry);
		}

		@Override
		protected boolean copyIssuedAttributeValuesToNonIssued(final AttributeGUID attributeGUID)
		{
			return false;
		}

	}

	/**
	 * JAXB/XACML &lt;Attributes&gt; parser
	 */
	public interface JaxbXACMLAttributesParser
	{
		/**
		 * Parses XACML Attributes element into internal Java type expected by/optimized for the policy decision engine
		 * 
		 * @param jaxbAttributes
		 *            Attributes element unmarshalled by JAXB. If the result of this method is not null, this parameter
		 *            is changed by this method to the final Attributes to be included in the final Result, i.e. all
		 *            Attribute elements with IncludeInresult = false and the Content are removed after this method
		 *            returns (a non-null result).
		 * @param xPathCompiler
		 *            XPath compiler for compiling/evaluating XPath expressions in values, such as XACML
		 *            xpathExpressions, typically derived from XACML RequestDefaults/XPathVersion
		 * @return Attributes parsing result; null if nothing to parse, i.e. no Attribute and (no Content or Content
		 *         parsing disabled);
		 * @throws IndeterminateEvaluationException
		 *             if any parsing error occurs
		 */
		SingleCategoryAttributes<?> parseAttributes(Attributes jaxbAttributes, XPathCompiler xPathCompiler)
				throws IndeterminateEvaluationException;
	}

	/**
	 * JAXB/XACML &lt,Attributes&gt; parser factory
	 * 
	 */
	interface JaxbXACMLAttributesParserFactory
	{

		/**
		 * Get instance of JAXB/XACML Attributes element parser
		 * 
		 * @return instance
		 */
		JaxbXACMLAttributesParser getInstance();
	}

	/**
	 * Base JAXB/XACML &lt;Attributes&gt; parser
	 * 
	 * @param <BAG>
	 *            type of bag resulting from parsing XACML AttributeValues
	 */
	private static abstract class BaseJaxbXACMLAttributesParser<BAG extends Iterable<? extends AttributeValue>>
			implements JaxbXACMLAttributesParser
	{
		private final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser;
		private final NamedAttributeIteratorConverter<BAG> namedAttrIterConverter;

		private BaseJaxbXACMLAttributesParser(final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser,
				final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
		{
			assert jaxbAttributeParser != null && namedAttributeIteratorConverter != null;

			this.jaxbAttributeParser = jaxbAttributeParser;
			this.namedAttrIterConverter = namedAttributeIteratorConverter;
		}

		/**
		 * Parse &lt;Attributes&gt; to XPath data model for XPath evaluation
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
		protected abstract XdmNode parseContent(String categoryName, Content jaxbContent)
				throws IndeterminateEvaluationException;

		@Override
		public SingleCategoryAttributes<BAG> parseAttributes(final Attributes jaxbAttributes,
				final XPathCompiler xPathCompiler) throws IndeterminateEvaluationException
		{
			assert jaxbAttributes != null;

			final String categoryName = jaxbAttributes.getCategory();
			final List<Attribute> categoryAttrs = jaxbAttributes.getAttributes();
			final Content jaxbAttrsContent = jaxbAttributes.getContent();
			final XdmNode extraContent = parseContent(categoryName, jaxbAttrsContent);
			if (categoryAttrs.isEmpty() && extraContent == null)
			{
				/*
				 * Skipping this <Attributes> because no <Attribute> and no extra Content parsed
				 */
				return null;
			}

			/*
			 * Ignore jaxbAttrCategory.getId(), as it is primarily intended to be referenced in multiple requests when
			 * implementing MultiRequests of Multiple Decision Profile, not implemented here.
			 */

			/*
			 * Let's iterate over the attributes to convert the list to a map indexed by the attribute
			 * category/id/issuer for quicker access during request evaluation. There might be multiple occurrences of
			 * <Attribute> with same meta-data (id, etc.), so the map value type need to be expandable/appendable to
			 * merge new values when new occurrences are found, e.g. Collection.
			 */
			final Map<AttributeGUID, BAG> attrMap = HashObjObjMaps.newUpdatableMap();

			/*
			 * categoryAttrs is immutable (JAXB-annotated classes have been generated as such using -immutable arg) so
			 * we cannot modify it directly to create the list of Attributes included in Result (IncludeInResult=true)
			 */
			final List<Attribute> returnedAttributes = new ArrayList<>(categoryAttrs.size());
			final Iterator<Attribute> categoryAttrsIterator = categoryAttrs.iterator();
			while (categoryAttrsIterator.hasNext())
			{
				final Attribute jaxbAttr = categoryAttrsIterator.next();
				final AttributeGUID attrGUID = new AttributeGUID(categoryName, jaxbAttr.getIssuer(),
						jaxbAttr.getAttributeId());

				// The XACML schema specifies there should be at least one AttributeValue
				final List<AttributeValueType> jaxbAttrValues = jaxbAttr.getAttributeValues();
				if (jaxbAttrValues.isEmpty())
				{
					throw new IndeterminateEvaluationException(
							"Missing AttributeValue(s) for Attribute " + attrGUID + " (cf. XACML 3.0 schema)",
							StatusHelper.STATUS_SYNTAX_ERROR);
				}

				/*
				 * Update the attribute map with new values resulting from parsing the new XACML AttributeValues
				 */
				try
				{
					jaxbAttributeParser.parseAttribute(attrMap, attrGUID, jaxbAttrValues, xPathCompiler);
				}
				catch (final IllegalArgumentException e)
				{
					throw new IndeterminateEvaluationException("Invalid Attributes/Attribute element",
							StatusHelper.STATUS_SYNTAX_ERROR, e);
				}

				// Check IncludeInResult
				if (jaxbAttr.isIncludeInResult())
				{
					returnedAttributes.add(jaxbAttr);
				}

			}

			/*
			 * If there are Attributes to include, create an <Attributes> with these but without Content to include in
			 * the Result.
			 */
			final Attributes attrsToIncludeInResult = returnedAttributes.isEmpty() ? null
					: new Attributes(null, returnedAttributes, jaxbAttributes.getCategory(), jaxbAttributes.getId());
			return new SingleCategoryAttributes<>(attrMap.entrySet(), namedAttrIterConverter, attrsToIncludeInResult,
					extraContent);
		}
	}

	private static final class ContentSkippingJaxbXACMLAttributesParser<BAG extends Iterable<? extends AttributeValue>>
			extends BaseJaxbXACMLAttributesParser<BAG>
	{
		private ContentSkippingJaxbXACMLAttributesParser(final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser,
				final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
		{
			super(jaxbAttributeParser, namedAttributeIteratorConverter);

			assert jaxbAttributeParser != null && namedAttributeIteratorConverter != null;
		}

		@Override
		protected XdmNode parseContent(final String categoryName, final Content jaxbContent)
				throws IndeterminateEvaluationException
		{
			// Content parsing not supported
			return null;
		}
	}

	private static final IllegalArgumentException NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined namedAttributeIteratorConverter");
	private static final IllegalArgumentException NULL_JAXB_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION = new IllegalArgumentException(
			"Undefined JAXB/XACML Attribute parser (null jaxbAttributeParser)");

	/**
	 * 
	 * Factory for JAXB/XACML &lt;Attributes&gt; parser that only parses the named attributes (Attribute elements), not
	 * the Content
	 * 
	 * @param <BAG>
	 *            resulting from parsing XACML AttributeValues
	 */
	public static final class ContentSkippingJaxbXACMLAttributesParserFactory<BAG extends Iterable<? extends AttributeValue>>
			implements JaxbXACMLAttributesParserFactory
	{
		private final JaxbXACMLAttributesParser instance;

		/**
		 * Creates instance
		 * 
		 * @param jaxbAttributeParser
		 *            parser used to parse each JAXB/XACML &lt;Attribute&gt;
		 * @param namedAttributeIteratorConverter
		 *            converts iterator over attributes with values produced by {@code jaxbAttributeParser}, into
		 *            constant-valued/immutable attribute iterator
		 * @throws IllegalArgumentException
		 *             {@code if(jaxbAttributeParser == null || namedAttributeIteratorConverter == null)}
		 */
		public ContentSkippingJaxbXACMLAttributesParserFactory(final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser,
				final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter)
				throws IllegalArgumentException
		{
			if (jaxbAttributeParser == null)
			{
				throw NULL_JAXB_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION;
			}

			if (namedAttributeIteratorConverter == null)
			{
				throw NULL_NAMED_ATTRIBUTE_ITERATOR_CONVERTER_ARGUMENT_EXCEPTION;
			}

			instance = new ContentSkippingJaxbXACMLAttributesParser<>(jaxbAttributeParser,
					namedAttributeIteratorConverter);
		}

		@Override
		public JaxbXACMLAttributesParser getInstance()
		{
			return instance;
		}

	}

	private static final class FullJaxbXACMLAttributesParser<BAG extends Iterable<? extends AttributeValue>>
			extends BaseJaxbXACMLAttributesParser<BAG>
	{
		// XML document builder for parsing Content to XPath data model for XPath evaluation
		private final DocumentBuilder xmlDocBuilder;

		private FullJaxbXACMLAttributesParser(final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser,
				final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter,
				final DocumentBuilder xmlDocBuilder)
		{
			super(jaxbAttributeParser, namedAttributeIteratorConverter);
			assert jaxbAttributeParser != null && namedAttributeIteratorConverter != null && xmlDocBuilder != null;
			this.xmlDocBuilder = xmlDocBuilder;
		}

		@Override
		public XdmNode parseContent(final String categoryName, final Content jaxbContent)
				throws IndeterminateEvaluationException
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
				throw new IndeterminateEvaluationException("Invalid Content of Attributes[@Category=" + categoryName
						+ "] for XPath evaluation: no child element", StatusHelper.STATUS_SYNTAX_ERROR);
			}

			try
			{
				return xmlDocBuilder.wrap(childElt);
			}
			catch (final IllegalArgumentException e)
			{
				throw new IndeterminateEvaluationException(
						"Error parsing Content of Attributes[@Category=" + categoryName + "] for XPath evaluation",
						StatusHelper.STATUS_SYNTAX_ERROR, e);
			}

		}

	}

	/**
	 * 
	 * Factory for JAXB/XACML &lt;Attributes&gt; Parser that parses the named attributes (Attribute elements), and the
	 * free-form Content
	 * 
	 * @param <BAG>
	 *            resulting from parsing XACML AttributeValues
	 */
	public static final class FullJaxbXACMLAttributesParserFactory<BAG extends Iterable<? extends AttributeValue>>
			implements JaxbXACMLAttributesParserFactory
	{
		private static final IllegalArgumentException NULL_XML_PROCESSOR_ARGUMENT_EXCEPTION = new IllegalArgumentException(
				"Undefined XML processor (null xmlProcessor)");
		private final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser;
		private final NamedAttributeIteratorConverter<BAG> namedAttrIterConverter;
		private final Processor xmlProc;

		/**
		 * Creates instance
		 * 
		 * @param jaxbAttributeParser
		 *            parser used to parse each JAXB/XACML &lt;Attribute&gt;
		 * @param namedAttributeIteratorConverter
		 *            converts iterator over attributes with values produced by {@code jaxbAttributeParser}, into
		 *            constant-valued/immutable attribute iterator
		 * @param xmlProcessor
		 *            SAXON XML processor to process the Attributes/Content node
		 * @throws IllegalArgumentException
		 *             {@code if(jaxbAttributeParser == null || namedAttributeIteratorConverter == null || xmlProcessor == null)}
		 */
		public FullJaxbXACMLAttributesParserFactory(final JaxbXACMLAttributeParser<BAG> jaxbAttributeParser,
				final NamedAttributeIteratorConverter<BAG> namedAttributeIteratorConverter,
				final Processor xmlProcessor)
		{
			if (jaxbAttributeParser == null)
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

			this.jaxbAttributeParser = jaxbAttributeParser;
			this.namedAttrIterConverter = namedAttributeIteratorConverter;
			this.xmlProc = xmlProcessor;
		}

		@Override
		public JaxbXACMLAttributesParser getInstance()
		{
			// create instance of inner class (has access to this.xmlProc)
			return new FullJaxbXACMLAttributesParser<>(jaxbAttributeParser, namedAttrIterConverter,
					xmlProc.newDocumentBuilder());
		}
	}

	private JaxbXACMLUtils()
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
