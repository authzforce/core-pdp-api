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
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeCategory;
import org.ow2.authzforce.xacml.identifiers.XacmlAttributeId;
import org.ow2.authzforce.xacml.identifiers.XacmlResourceScope;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * XACML Request Attribute parser that is aware of all named attributes parsed in the request. This kind of parser has side effects as it modifies/updates a map of attributes that can be passed to the
 * parser from one call to another, for parsing a whole set of request attributes.
 * 
 * @param <INPUT_ATTRIBUTE>
 *            type of attribute object from input XACML Request, e.g. JAXB-annotated Attribute for XACML/XML request or JSON object for XACML/JSON request
 *
 * @param <BAG>
 *            type of attribute value bag resulting from parsing the AttributeValues
 */
public abstract class XacmlRequestAttributeParser<INPUT_ATTRIBUTE, BAG extends Iterable<? extends AttributeValue>>
{
	private static final AttributeFqn RESOURCE_SCOPE_ATTRIBUTE_GUID = AttributeFqns.newInstance(XacmlAttributeCategory.XACML_3_0_RESOURCE.value(), Optional.empty(),
			XacmlAttributeId.XACML_2_0_RESOURCE_SCOPE.value());
	private static final AttributeValue IMMEDIATE_RESOURCE_SCOPE_ATTRIBUTE_VALUE = new StringValue(XacmlResourceScope.IMMEDIATE.value());

	private static final IllegalArgumentException INVALID_MULTIPLE_SCOPE_VALUE_EXCEPTION = new IllegalArgumentException("Invalid value of attribute '" + RESOURCE_SCOPE_ATTRIBUTE_GUID
			+ "': none or more than one string. Expected: one and only one string.");

	private static final IllegalArgumentException UNSUPPORTED_MULTIPLE_SCOPE_VALUE_EXCEPTION = new IllegalArgumentException("Unsupported value of attribute '" + RESOURCE_SCOPE_ATTRIBUTE_GUID
			+ "'. Expected: undefined or standard XACML string '" + XacmlResourceScope.IMMEDIATE.value() + "'. (Other values are not supported.)");

	private static final IllegalArgumentException NULL_NAMED_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION = new IllegalArgumentException("Undefined parser for input XACML named attributes");

	/**
	 * Validates the 'scope' attribute as defined in Multiple Decision Profile (§2.1 and §5)
	 * 
	 * @param attributeFQN
	 *            attribute name
	 * @param attributeValues
	 *            attribute values
	 * @throws IllegalArgumentException
	 *             if the values are not actually the singleton string "Immediate" (other values are not supported)
	 */
	protected static void validateResourceScope(final AttributeFqn attributeFQN, final Iterable<? extends AttributeValue> attributeValues) throws IllegalArgumentException
	{
		assert attributeFQN != null && attributeValues != null;
		/*
		 * Check whether this is not an unsupported resource-scope attribute. XACML Multiple Decision Profile, § 2.3.3: "... If such a <Attributes> element contains a 'scope' attribute having any
		 * value other than 'Immediate', then the Individual Request SHALL be further processed according to the processing model specified in Section 4." We do not support 'scope' other than
		 * 'Immediate' so throw an error if different.
		 */
		if (!attributeFQN.equals(RESOURCE_SCOPE_ATTRIBUTE_GUID))
		{
			return;
		}

		final Iterator<? extends AttributeValue> valIterator = attributeValues.iterator();
		if (!valIterator.hasNext())
		{
			throw INVALID_MULTIPLE_SCOPE_VALUE_EXCEPTION;
		}

		final AttributeValue val = valIterator.next();
		if (!IMMEDIATE_RESOURCE_SCOPE_ATTRIBUTE_VALUE.equals(val))
		{
			throw UNSUPPORTED_MULTIPLE_SCOPE_VALUE_EXCEPTION;
		}

		if (valIterator.hasNext())
		{
			throw INVALID_MULTIPLE_SCOPE_VALUE_EXCEPTION;
		}
	}

	private final NamedXacmlAttributeParser<INPUT_ATTRIBUTE> namedAttParser;

	protected XacmlRequestAttributeParser(final NamedXacmlAttributeParser<INPUT_ATTRIBUTE> namedAttributeParser) throws IllegalArgumentException
	{
		if (namedAttributeParser == null)
		{
			throw NULL_NAMED_ATTRIBUTE_PARSER_ARGUMENT_EXCEPTION;
		}

		this.namedAttParser = namedAttributeParser;
	}

	/**
	 * Parse a given named attribute alone.
	 * 
	 * @param inputXacmlAttribute
	 *            input attribute object (not yet parsed into AuthzForce internal model), typically from original XACML request
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, such as XACML xpathExpressions. Undefined if XPath support disabled (by PDP configuration of RequestDefaults/XPathVersion undefined).
	 * 
	 * @throws IllegalArgumentException
	 *             if parsing of the {@code inputXacmlAttribute} failed because of invalid syntax, e.g. invalid datatype or mixing different datatypes
	 */
	protected final NamedXacmlAttributeParsingResult<?> parseNamedAttribute(final String attributeCategoryId, final INPUT_ATTRIBUTE inputXacmlAttribute, final Optional<XPathCompilerProxy> xPathCompiler)
			throws IllegalArgumentException
	{
		return this.namedAttParser.parseNamedAttribute(attributeCategoryId, inputXacmlAttribute, xPathCompiler);
	}

	/**
	 * Parse a given named attribute.
	 * 
	 * @param attributeCategoryId
	 *            attribute category ID
	 * 
	 * @param attributeMap
	 *            request attribute map to be updated by the result of parsing {@code inputXacmlAttribute}
	 * @param inputXacmlAttribute
	 *            input attribute object (not yet parsed into AuthzForce internal model), typically from original XACML request
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, such as XACML xpathExpressions
	 * 
	 * @throws IllegalArgumentException
	 *             if parsing of the {@code inputXacmlAttribute} failed because of invalid syntax, e.g. invalid datatype or mixing different datatypes
	 */
	public abstract void parseNamedAttribute(String attributeCategoryId, INPUT_ATTRIBUTE inputXacmlAttribute, Optional<XPathCompilerProxy> xPathCompiler, Map<AttributeFqn, BAG> attributeMap)
			throws IllegalArgumentException;
}