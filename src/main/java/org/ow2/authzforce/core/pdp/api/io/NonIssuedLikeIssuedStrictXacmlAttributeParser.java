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

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeSources;
import org.ow2.authzforce.core.pdp.api.expression.XPathCompilerProxy;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;
import org.ow2.authzforce.core.pdp.api.value.Bags;

import java.util.Map;
import java.util.Optional;

/**
 * On the contrary to {@link IssuedToNonIssuedCopyingLaxXacmlAttributeParser}, this XACML Attribute parser does not copy the values of Attributes having an Issuer to the corresponding Attributes
 * without Issuer (same Category, AttributeId...) in the resulting attribute map. Therefore, it does not comply with what XACML 3.0, §5.29 says on &lt;AttributeDesignator&gt; evaluation. However, it is
 * more performant. In this implementation, an Attribute with no Issuer is handled like an attribute with an Issuer, except the Issuer has the special value "null". Therefore, an AttributeDesignator
 * with "null" Issuer (undefined) will still match any attribute in the request with "null" Issuer (but not any other Attribute with same AttributeId but a defined/non-null Issuer, for which a
 * different AttributeDesignator with a defined Issuer must be used).
 * <p>
 * "Strict" means it does not allow defining multivalued attributes by repeating the same XACML Attribute (same AttributeId) within a XACML Attributes element (same Category). This is not fully
 * compliant with the XACML spec according to a discussion on the xacml-dev mailing list (see {@linkplain "https://lists.oasis-open.org/archives/xacml-dev/201507/msg00001.html"}), referring to the
 * XACML 3.0 core spec, §7.3.3, that indicates that multiple occurrences of the same &lt;Attribute&gt; with same meta-data but different values should be considered equivalent to a single
 * &lt;Attribute&gt; element with same meta-data and merged values (multivalued Attribute). Moreover, the XACML 3.0 conformance test 'IIIA024' expects this behavior: the multiple subject-id
 * Attributes are expected to result in a multi-value bag during evaluation of the &lt;AttributeDesignator&gt;.
 * <p>
 * In a nutshell, this type of attribute parser does not comply fully with XACML 3.0. However, to benefit fully from the XACML capabilities, it is strongly recommended to avoid such Attribute
 * repetitions and group all the values of the same Attribute in the same Attribute element with multiple AttributeValues. In that case, you will achieve better performances by using this "strict"
 * parser instead of the "lax" version.
 * 
 * @param <INPUT_ATTRIBUTE>
 *            type of raw input attribute object (not yet parsed into AuthzForce internal model), typically from original XACML Request, e.g. JAXB-annotated Attribute for XACML/XML request, or JSON
 *            object for XACML/JSON request
 *
 */
public final class NonIssuedLikeIssuedStrictXacmlAttributeParser<INPUT_ATTRIBUTE> extends XacmlRequestAttributeParser<INPUT_ATTRIBUTE, AttributeBag<?>>
{
	/**
	 * Creates instance of XACML Attribute Parser
	 * 
	 * @param namedAttributeParser
	 *            low-level parser for named attributes of type {@code INPUT_ATTRIBUTE}
	 * @throws IllegalArgumentException
	 *             iff {@code namedAttributeParser == null}
	 */
	public NonIssuedLikeIssuedStrictXacmlAttributeParser(final NamedXacmlAttributeParser<INPUT_ATTRIBUTE> namedAttributeParser) throws IllegalArgumentException
	{
		super(namedAttributeParser);
	}

	private static <AV extends AttributeValue> AttributeBag<AV> newAttributeBag(final NamedXacmlAttributeParsingResult<AV> result)
	{
		return Bags.newAttributeBag(result.getAttributeDatatype(), result.getAttributeValues(), AttributeSources.REQUEST);
	}

	/**
	 * "Strict" parsing method, that parse all the values of a given attribute in one call. In short, this method will reject multiple calls on the same Attribute identifier (same metadata).
	 * 
	 * @param attributeMap
	 *            request attribute map to be updated by the result of parsing {@code inputXacmlAttribute}
	 * @param inputXacmlAttribute
	 *            input attribute object (not yet parsed into AuthzForce internal model), typically from original XACML request
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, such as XACML xpathExpressions. Undefined if XPath support disabled.
	 * 
	 * @throws IllegalArgumentException
	 *             if parsing of the {@code inputXacmlAttribute} because of invalid datatype or mixing of different datatypes; or if there are already existing values for the same attribute
	 *             (repetition of same attribute is not allowed in strict mode)
	 */
	@Override
	public void parseNamedAttribute(final String attributeCategoryId, final INPUT_ATTRIBUTE inputXacmlAttribute, final Optional<XPathCompilerProxy> xPathCompiler,
			final Map<AttributeFqn, AttributeBag<?>> attributeMap) throws IllegalArgumentException
	{
		final NamedXacmlAttributeParsingResult<?> attParsingResult = parseNamedAttribute(attributeCategoryId, inputXacmlAttribute, xPathCompiler);
		final AttributeBag<?> attBag = newAttributeBag(attParsingResult);

		/*
		 * If there is any existing values for the same attribute name (<Attribute> with same meta-data) in the map, it will be rejected. This behavior is not fully compliant with XACML (see the
		 * Javadoc of this class), however it is faster than the compliant alternative.
		 */
		final AttributeFqn attName = attParsingResult.getAttributeName();
		final Bag<?> duplicate = attributeMap.putIfAbsent(attName, attBag);
		if (duplicate != null)
		{
			throw new IllegalArgumentException("Illegal syntax in strict mode: duplicate <Attribute> with metadata: " + attParsingResult.getAttributeName());
		}

		/*
		 * Check if it is a resource-scope.
		 */
		validateResourceScope(attName, attBag);

		/*
		 * In this implementation, we do not comply fully with XACML 3.0, §5.29, since we handle Attribute(s) without Issuer exactly like the ones with an Issuer. In other words, an undefined issuer
		 * is handled like the special "null" Issuer. Therefore, an AttributeDesignators without Issuer will not match the request attributes with matching Category, AttributeId... but a defined
		 * therefore different Issuer. It will only match the request attribute without Issuer. In a compliant implementation, we would check if the attribute has an Issuer, and if it does, also
		 * update the attribute variant with same meta-data except no Issuer.
		 */
	}

}