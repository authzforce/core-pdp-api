/**
 * Copyright 2012-2021 THALES.
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

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Parser for XACML request attribute categories, i.e. group of attributes in a specific category, e.g. XACML/XML (JAXB) Attributes element, XACML/JSON (JSON Profile) Category object.
 * 
 * @param <INPUT_ATTRIBUTE_CATEGORY>
 *            type of category-specific attribute group in the original XACML request, e.g. XACML/XML (JAXB) Attributes element, XACML/JSON (JSON Profile) Category object.
 */
public interface SingleCategoryXacmlAttributesParser<INPUT_ATTRIBUTE_CATEGORY>
{
	/**
	 * Parses XACML Attributes element into internal Java type expected by/optimized for the policy decision engine
	 * 
	 * @param inputXacmlAttributeCategory
	 *            category of attributes from original XACML request, ie. XACML Attributes element.
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, such as XACML xpathExpressions, typically derived from XACML RequestDefaults/XPathVersion
	 * @return Attributes parsing result; null if nothing to parse, i.e. no Attribute and (no Content or Content parsing disabled);
	 * @throws IndeterminateEvaluationException
	 *             if any parsing error occurs
	 */
	SingleCategoryAttributes<?, INPUT_ATTRIBUTE_CATEGORY> parseAttributes(INPUT_ATTRIBUTE_CATEGORY inputXacmlAttributeCategory, XPathCompiler xPathCompiler) throws IndeterminateEvaluationException;

	/**
	 * Factory of XACML attribute category parser
	 * 
	 * @param <INPUT_ATTRIBUTE_CATEGORY>
	 *            type of category-specific attribute group in the original XACML request, e.g. XACML/XML (JAXB) Attributes element, XACML/JSON (JSON Profile) Category object.
	 */
	interface Factory<INPUT_ATTRIBUTE_CATEGORY>
	{

		/**
		 * Get instance of XACML Attributes parser
		 * 
		 * @return instance
		 */
		SingleCategoryXacmlAttributesParser<INPUT_ATTRIBUTE_CATEGORY> getInstance();
	}
}