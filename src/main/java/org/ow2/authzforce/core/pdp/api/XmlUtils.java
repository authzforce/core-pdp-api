/**
 * Copyright 2012-2018 Thales Services SAS.
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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.ow2.authzforce.core.pdp.api.io.XacmlJaxbParsingUtils;
import org.ow2.authzforce.xacml.identifiers.XPathVersion;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Utilities for XML-to-JAXB binding
 * 
 */
public final class XmlUtils
{
	/**
	 * XML datatype factory for parsing XML-Schema-compliant date/time/duration values into Java types. DatatypeFactory's official javadoc does not say whether it is thread-safe. But bug report
	 * indicates it should be and has been so far: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177 Reusing the same instance matters for performance: https://www.java.net/node/666491 The
	 * alternative would be to use ThreadLocal to limit thread-safety issues in the future.
	 */
	public static final DatatypeFactory XML_TEMPORAL_DATATYPE_FACTORY;
	static
	{
		try
		{
			XML_TEMPORAL_DATATYPE_FACTORY = DatatypeFactory.newInstance();
		}
		catch (final DatatypeConfigurationException e)
		{
			throw new RuntimeException("Error instantiating XML datatype factory for parsing strings corresponding to XML schema date/time/duration values into Java types", e);
		}
	}

	/**
	 * Saxon configuration file for Attributes/Content XML parsing (into XDM data model) and AttributeSelector's XPath evaluation
	 */
	public static final String SAXON_CONFIGURATION_CLASSPATH_LOCATION = "saxon.xml";

	/**
	 * SAXON XML/XPath Processor configured by {@value #SAXON_CONFIGURATION_CLASSPATH_LOCATION}
	 */
	public static final Processor SAXON_PROCESSOR;
	static
	{
		final ClassLoader classLoader = XacmlJaxbParsingUtils.class.getClassLoader();
		final URL saxonConfURL = classLoader.getResource(SAXON_CONFIGURATION_CLASSPATH_LOCATION);
		if (saxonConfURL == null)
		{
			throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_CLASSPATH_LOCATION);
		}

		try
		{
			SAXON_PROCESSOR = new Processor(new StreamSource(saxonConfURL.toString()));
		}
		catch (final SaxonApiException e)
		{
			throw new RuntimeException("Error loading Saxon processor from configuration file at this location: " + SAXON_CONFIGURATION_CLASSPATH_LOCATION, e);
		}

		final Boolean isXincludeAware = (Boolean) SAXON_PROCESSOR.getConfigurationProperty(FeatureKeys.XINCLUDE);
		if (isXincludeAware)
		{
			/**
			 * xInclude=true is not compatible with FullJaxbXACMLAttributesParser#parseContent(), causes error:
			 * <p>
			 * net.sf.saxon.s9api.SaxonApiException: Selected XML parser javax.xml.bind.util.JAXBSource$1 does not recognize request for XInclude processing
			 * <p>
			 * at net.sf.saxon.s9api.DocumentBuilder.build(DocumentBuilder.java:374) ~[Saxon-HE-9.6.0-5.jar:na]
			 * <p>
			 * at org.ow2.authzforce.core.XACMLParsers$FullJaxbXACMLAttributesParserFactory$FullJaxbXACMLAttributesParser.parseContent(XACMLParsers.java:909) ~[classes/:na]
			 */
			throw new UnsupportedOperationException("Error loading Saxon processor from configuration file at this location: " + SAXON_CONFIGURATION_CLASSPATH_LOCATION
					+ ": xInclude=true is not supported. Please remove any 'xInclude' parameter from this configuration file.");
		}
	}

	private static final IllegalArgumentException NULL_NAMESPACE_PREFIX_EXCEPTION = new IllegalArgumentException("Invalid XPath compiler input: null namespace prefix in namespace prefix-URI mappings");
	private static final IllegalArgumentException NULL_NAMESPACE_URI_EXCEPTION = new IllegalArgumentException("Invalid XPath compiler input: null namespace URI in namespace prefix-URI mappings");

	private static XPathCompiler newXPathCompiler(final XPathVersion xpathVersion) throws IllegalArgumentException
	{
		final XPathCompiler xpathCompiler = SAXON_PROCESSOR.newXPathCompiler();
		xpathCompiler.setLanguageVersion(xpathVersion.getVersionNumber());
		/*
		 * No need for caching since we are only using this for XPaths in Policy/PolicySet (AttributeSelector and xpathExpression), not in the Request (not supported)
		 */
		xpathCompiler.setCaching(false);
		xpathCompiler.setSchemaAware(false);
		return xpathCompiler;
	}

	// Default XPath compilers by XPathVersion outside any namespace context
	private static final Map<String, XPathCompiler> XPATH_COMPILERS_BY_VERSION = HashCollections.newImmutableMap(
	// XPATH 1.0 compiler
			XPathVersion.V1_0.getURI(), newXPathCompiler(XPathVersion.V1_0),
			// XPATH 2.0 compiler
			XPathVersion.V2_0.getURI(), newXPathCompiler(XPathVersion.V2_0));

	/**
	 * Create XPath compiler for given XPath version and namespace context. For single evaluation of a given XPath with {@link XPathCompiler#evaluateSingle(String, XdmItem)}. For repeated evaluation
	 * of the same XPath, use {@link XPathEvaluator} instead. What we have in XACML Policy/PolicySetDefaults is the version URI so we need this map to map the URI to the XPath compiler
	 * 
	 * @param xpathVersionURI
	 *            XPath version URI, e.g. "http://www.w3.org/TR/1999/REC-xpath-19991116"
	 * @param namespaceURIsByPrefix
	 *            namespace prefix-URI mapping to be part of the static context for XPath expressions compiled using the created XPathCompiler
	 * @return XPath compiler instance
	 * @throws IllegalArgumentException
	 *             if {@code xpathVersionURI} is invalid or unsupported XPath version or one of the namespace prefixes/URIs in {@code namespaceURIsByPrefix} is null
	 */
	public static XPathCompiler newXPathCompiler(final String xpathVersionURI, final Map<String, String> namespaceURIsByPrefix) throws IllegalArgumentException
	{
		if (namespaceURIsByPrefix == null || namespaceURIsByPrefix.isEmpty())
		{
			final XPathCompiler xpathCompiler = XPATH_COMPILERS_BY_VERSION.get(xpathVersionURI);
			if (xpathCompiler == null)
			{
				throw new IllegalArgumentException("Invalid or unsupported XPathVersion: " + xpathVersionURI);
			}

			return xpathCompiler;
		}

		final XPathVersion xpathVersion = XPathVersion.fromURI(xpathVersionURI);
		final XPathCompiler xpathCompiler = newXPathCompiler(xpathVersion);
		for (final Entry<String, String> nsPrefixToURI : namespaceURIsByPrefix.entrySet())
		{
			final String prefix = nsPrefixToURI.getKey();
			final String uri = nsPrefixToURI.getValue();
			if (prefix == null)
			{
				throw NULL_NAMESPACE_PREFIX_EXCEPTION;
			}

			if (uri == null)
			{
				throw NULL_NAMESPACE_URI_EXCEPTION;
			}

			xpathCompiler.declareNamespace(prefix, uri);
		}

		return xpathCompiler;
	}

	/**
	 * Wrapper around XPathExecutable that provides the original XPath expression from which the XPathExecutable was compiled, via toString() method. To be used for XPath-based Expression evaluations,
	 * e.g. AttributeSelector, xpathExpression, etc.
	 */
	public static final class XPathEvaluator
	{
		private final XPathExecutable exec;
		private final String expr;

		/**
		 * Creates instance
		 * 
		 * @param path
		 *            XPath executable
		 * @param xPathCompiler
		 *            XPath compiler
		 * @throws IllegalArgumentException
		 *             in case of invalid XPath
		 */
		public XPathEvaluator(final String path, final XPathCompiler xPathCompiler) throws IllegalArgumentException
		{
			try
			{
				this.exec = xPathCompiler.compile(path);
			}
			catch (final SaxonApiException e)
			{
				throw new IllegalArgumentException(this + ": Invalid XPath", e);
			}

			this.expr = path;
		}

		@Override
		public String toString()
		{
			return expr;
		}

		/**
		 * @return An XPathSelector. The returned XPathSelector can be used to set up the dynamic context, and then to evaluate the expression.
		 * @see XPathExecutable#load()
		 */
		public XPathSelector load()
		{
			return exec.load();
		}
	}

	/**
	 * (Namespace-filtering) XML-to-JAXB parser
	 *
	 */
	public interface XmlnsFilteringParser
	{

		/**
		 * Unmarshal XML data from the specified SAX InputSource and return the resulting content tree.
		 * 
		 * @param source
		 *            the input source to unmarshal XML data from
		 * @return the newly created root object of the java content tree
		 * @throws JAXBException
		 *             If any unexpected errors occur while unmarshalling
		 * @throws IllegalArgumentException
		 *             if {@code source} is null
		 */
		Object parse(InputSource source) throws JAXBException, IllegalArgumentException;

		/**
		 * Unmarshal XML data from the specified URL and return the resulting content tree.
		 * 
		 * @param url
		 *            the URL to unmarshal XML data from
		 * @return the newly created root object of the java content tree
		 * @throws JAXBException
		 *             If any unexpected errors occur while unmarshalling
		 * @throws IllegalArgumentException
		 *             if {@code url} is null or invalid
		 */
		Object parse(URL url) throws JAXBException, IllegalArgumentException;

		/**
		 * Provides namespace prefix-URI mappings found during last call to {@link #parse(InputSource)}, if namespace prefix-URI collecting is supported. Such mappings may then be used for
		 * namespace-aware XPath evaluation (e.g. XACML xpathExpression values)
		 * 
		 * @return namespace prefix-URI mappings; empty if {@link #parse(InputSource)} not called yet, or namespace prefix-URI collecting is not supported
		 */
		Map<String, String> getNamespacePrefixUriMap();

	}

	/**
	 * SAX-based namespace-filtering XML-to-JAXB parser.
	 *
	 */
	public static final class SAXBasedXmlnsFilteringParser implements XmlnsFilteringParser
	{
		private static final IllegalArgumentException NULL_ARG_EXCEPTION = new IllegalArgumentException("Undefined input XML");

		private static final SAXParserFactory NS_AWARE_SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
		static
		{
			NS_AWARE_SAX_PARSER_FACTORY.setNamespaceAware(true);
		}

		private final UnmarshallerHandler unmarshallerHandler;
		private final Map<String, String> nsPrefixUriMap = HashCollections.newUpdatableMap();
		private final XMLFilterImpl xmlFilter;

		/**
		 * Creates instance from JAXB unmarshaller used for parsing XML documents
		 * 
		 * @param unmarshaller
		 *            JAXB unmarshaller
		 */
		public SAXBasedXmlnsFilteringParser(final Unmarshaller unmarshaller)
		{
			final XMLReader xmlReader;
			try
			{
				xmlReader = NS_AWARE_SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
			}
			catch (SAXException | ParserConfigurationException e)
			{
				// fatal error: there is no way to use the SAXParserFactory at this point for anything
				throw new RuntimeException("Unable to create any XML parser from SAXParserFactory (required for namespace-aware XPath evaluation in particular)", e);
			}

			this.xmlFilter = new XMLFilterImpl(xmlReader)
			{

				@Override
				public void startPrefixMapping(final String prefix, final String uri) throws SAXException
				{
					final String duplicate = nsPrefixUriMap.putIfAbsent(prefix, uri);
					if (duplicate != null)
					{
						throw new RuntimeException("Duplicate declaration of namespace prefix '" + prefix + "' (empty string refers to default namespace)");
					}

					super.startPrefixMapping(prefix, uri);
				}

			};

			this.unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
			this.xmlFilter.setContentHandler(unmarshallerHandler);
		}

		@Override
		public Object parse(final InputSource input) throws JAXBException
		{
			if (input == null)
			{
				throw NULL_ARG_EXCEPTION;
			}

			this.nsPrefixUriMap.clear();
			try
			{
				this.xmlFilter.parse(input);
			}
			catch (SAXException | IOException e)
			{
				throw new JAXBException(e);
			}

			return this.unmarshallerHandler.getResult();
		}

		@Override
		public Object parse(final URL url) throws JAXBException
		{
			if (url == null)
			{
				throw NULL_ARG_EXCEPTION;
			}

			return parse(new InputSource(url.toExternalForm()));
		}

		@Override
		public Map<String, String> getNamespacePrefixUriMap()
		{
			return HashCollections.newImmutableMap(this.nsPrefixUriMap);
		}
	}

	/**
	 * This is a bare implementation of namespace-filtering parser, i.e. the result {@link #getNamespacePrefixUriMap()} is always empty (no namespace-prefix mappings is returned). Therefore it can be
	 * used as a convenient replacement for {@link SAXBasedXmlnsFilteringParser} when no namespace-filtering is actually required but still a parser compliant with {@link XmlnsFilteringParser} for
	 * polymorphism purposes.
	 *
	 */
	public static final class NoXmlnsFilteringParser implements XmlnsFilteringParser
	{
		private final Unmarshaller unmarshaller;

		/**
		 * Creates instance from JAXB unmarshaller used for parsing XML documents
		 * 
		 * @param unmarshaller
		 *            JAXB unmarshaller
		 */
		public NoXmlnsFilteringParser(final Unmarshaller unmarshaller)
		{
			this.unmarshaller = unmarshaller;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.NamespaceFilteringParser#parse(org.xml.sax.InputSource)
		 */
		@Override
		public Object parse(final InputSource input) throws JAXBException
		{
			return this.unmarshaller.unmarshal(input);
		}

		@Override
		public Object parse(final URL url) throws JAXBException
		{
			return this.unmarshaller.unmarshal(url);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.ow2.authzforce.core.NamespaceFilteringParser#getNamespacePrefixUriMap()
		 */
		@Override
		public Map<String, String> getNamespacePrefixUriMap()
		{
			return Collections.emptyMap();
		}
	}

	/**
	 * (Namespace-filtering) XACML-to-JAXB parser factory
	 *
	 */
	public interface XmlnsFilteringParserFactory
	{
		/**
		 * Get factory instance
		 * 
		 * @return instance
		 * @throws JAXBException
		 *             if any error instantiating XACML-to-JAXB parser
		 */
		XmlnsFilteringParser getInstance() throws JAXBException;
	}

	private XmlUtils()
	{
	}

	// SAXON PROCESSOR TESTING
	// public static void main(String[] args) throws SaxonApiException
	// {
	// Processor proc = new Processor(new StreamSource("src/test/resources/saxon.xml"));
	// System.out.println(SAXON_PROCESSOR.getConfigurationProperty(FeatureKeys.OCCURRENCE_LIMITS));
	// }
}
