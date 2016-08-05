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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
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

import org.ow2.authzforce.xacml.identifiers.XPATHVersion;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Utilities for XML-to-JAXB binding
 * 
 */
public final class XMLUtils
{

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
		final ClassLoader classLoader = JaxbXACMLUtils.class.getClassLoader();
		final URL saxonConfURL = classLoader.getResource(SAXON_CONFIGURATION_CLASSPATH_LOCATION);
		if (saxonConfURL == null)
		{
			throw new RuntimeException("No Saxon configuration file exists at default location: " + SAXON_CONFIGURATION_CLASSPATH_LOCATION);
		}

		try
		{
			SAXON_PROCESSOR = new Processor(new StreamSource(saxonConfURL.toString()));
		} catch (final SaxonApiException e)
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

	private static XPathCompiler newXPathCompiler(final XPATHVersion xpathVersion) throws IllegalArgumentException
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
	private static final Map<String, XPathCompiler> XPATH_COMPILERS_BY_VERSION;
	static
	{
		final Map<String, XPathCompiler> mutableMap = new HashMap<>();
		// XPATH 1.0 compiler
		mutableMap.put(XPATHVersion.V1_0.getURI(), newXPathCompiler(XPATHVersion.V1_0));
		// XPATH 2.0 compiler
		mutableMap.put(XPATHVersion.V2_0.getURI(), newXPathCompiler(XPATHVersion.V2_0));
		XPATH_COMPILERS_BY_VERSION = Collections.unmodifiableMap(mutableMap);
	}

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

		final XPATHVersion xpathVersion = XPATHVersion.fromURI(xpathVersionURI);
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
			} catch (final SaxonApiException e)
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
	public interface NamespaceFilteringParser
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
	public static final class SAXBasedNamespaceFilteringParser implements NamespaceFilteringParser
	{
		private static final IllegalArgumentException NULL_ARG_EXCEPTION = new IllegalArgumentException("Undefined input XML");

		private static final SAXParserFactory NS_AWARE_SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
		static
		{
			NS_AWARE_SAX_PARSER_FACTORY.setNamespaceAware(true);
		}

		private final UnmarshallerHandler unmarshallerHandler;
		private final Map<String, String> nsPrefixUriMap = new HashMap<>();
		private final XMLFilterImpl xmlFilter;

		/**
		 * Creates instance from JAXB unmarshaller used for parsing XML documents
		 * 
		 * @param unmarshaller
		 *            JAXB unmarshaller
		 */
		public SAXBasedNamespaceFilteringParser(final Unmarshaller unmarshaller)
		{
			final XMLReader xmlReader;
			try
			{
				xmlReader = NS_AWARE_SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
			} catch (SAXException | ParserConfigurationException e)
			{
				// fatal error: there is no way to use the SAXParserFactory at this point for anything
				throw new RuntimeException("Unable to create any XML parser from SAXParserFactory (required for namespace-aware XPath evaluation in particular)", e);
			}

			this.xmlFilter = new XMLFilterImpl(xmlReader)
			{

				@Override
				public void startPrefixMapping(final String prefix, final String uri) throws SAXException
				{
					nsPrefixUriMap.put(prefix, uri);
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
			} catch (SAXException | IOException e)
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
			return Collections.unmodifiableMap(this.nsPrefixUriMap);
		}
	}

	/**
	 * This is a bare implementation of namespace-filtering parser, i.e. the result {@link #getNamespacePrefixUriMap()} is always empty (no namespace-prefix mappings is returned). Therefore it can be
	 * used as a convenient replacement for {@link SAXBasedNamespaceFilteringParser} when no namespace-filtering is actually required but still a parser compliant with {@link NamespaceFilteringParser}
	 * for polymorphism purposes.
	 *
	 */
	public static final class NoNamespaceFilteringParser implements NamespaceFilteringParser
	{
		private final Unmarshaller unmarshaller;

		/**
		 * Creates instance from JAXB unmarshaller used for parsing XML documents
		 * 
		 * @param unmarshaller
		 *            JAXB unmarshaller
		 */
		public NoNamespaceFilteringParser(final Unmarshaller unmarshaller)
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

	private XMLUtils()
	{
	}

	// SAXON PROCESSOR TESTING
	// public static void main(String[] args) throws SaxonApiException
	// {
	// Processor proc = new Processor(new StreamSource("src/test/resources/saxon.xml"));
	// System.out.println(SAXON_PROCESSOR.getConfigurationProperty(FeatureKeys.OCCURRENCE_LIMITS));
	// }
}
