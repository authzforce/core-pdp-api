/*
 * Copyright 2012-2023 THALES.
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

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactory;
import org.ow2.authzforce.core.pdp.api.value.AttributeValueFactoryRegistry;

import java.util.Optional;

/**
 * Parser that parses named attributes in XACML
 *
 * @param <INPUT_ATTRIBUTE>
 *            type of attribute object from input XACML Request, e.g. JAXB-annotated Attribute for XACML/XML request or JSON object for XACML/JSON request
 */
public abstract class NamedXacmlAttributeParser<INPUT_ATTRIBUTE>
{
	private static final IllegalArgumentException NULL_ATTRIBUTE_VALUE_FACTORY_REGISTRY_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined registry of attribute value factories");

	private final AttributeValueFactoryRegistry datatypeFactoryRegistry;

	/**
	 * Constructor
	 * @param attributeValueFactoryRegistry registry of attribute value factories
	 * @throws IllegalArgumentException attributeValueFactoryRegistry is null
	 */
	protected NamedXacmlAttributeParser(final AttributeValueFactoryRegistry attributeValueFactoryRegistry) throws IllegalArgumentException
	{
		if (attributeValueFactoryRegistry == null)
		{
			throw NULL_ATTRIBUTE_VALUE_FACTORY_REGISTRY_ARGUMENT_EXCEPTION;
		}

		this.datatypeFactoryRegistry = attributeValueFactoryRegistry;
	}

	/**
	 * Get AttributeValueFactory for a given datatype ID
	 * @param attributeDatatypeId datatype ID
	 * @param attributeName attribute name for exception message
	 * @return attribute value factory
	 * @throws IllegalArgumentException if no factory exists for such datatype ID in the registry used by this parser
	 */
	protected final AttributeValueFactory<?> getAttributeValueFactory(final String attributeDatatypeId, final AttributeFqn attributeName) throws IllegalArgumentException
	{
		final AttributeValueFactory<?> datatypeFactory = datatypeFactoryRegistry.getExtension(attributeDatatypeId);
		if (datatypeFactory == null)
		{
			throw new IllegalArgumentException("Invalid Attribute '" + attributeName + "': invalid DataType: '" + attributeDatatypeId + "'");
		}

		return datatypeFactory;
	}

	/**
	 * Parse the input named attribute
	 * @param attributeCategoryId attribute category ID
	 * @param inputXacmlAttribute attribute
	 * @param xPathCompiler optional XPath compiler
	 * @return parsing result
	 * @throws IllegalArgumentException invalid inputXacmlAttribute or parsing error
	 */
	protected abstract NamedXacmlAttributeParsingResult<?> parseNamedAttribute(final String attributeCategoryId, final INPUT_ATTRIBUTE inputXacmlAttribute, final Optional<XPathCompilerProxy> xPathCompiler)
			throws IllegalArgumentException;
}