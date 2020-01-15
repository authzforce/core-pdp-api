/**
 * Copyright 2012-2020 THALES.
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
package org.ow2.authzforce.core.pdp.api.value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.PdpExtension;

/**
 * Datatype-specific Attribute Value Factory/Parser.
 * <p>
 * Note: In XACML, AttributeValues may be parsed from various sources: XACML Policy(Set), XACML Request, etc. Besides, there may be more than one possible factory/parser for a given datatype, e.g. if
 * there are different input data formats. For example, an XACML integer is represented as a XML text in XACML/XML core specification, and therefore results in a Java {@link String} with JAXB
 * framework; whereas in JSON Profile, it would be a JSON number and would result in Java {@link Number} with most JSON frameworks.
 * 
 * @param <AV>
 *            type of instance (attribute value) created by this factory
 */
public interface AttributeValueFactory<AV extends AttributeValue> extends PdpExtension
{
	/**
	 * Get datatype of values created by this factory
	 * 
	 * @return supported attribute value datatype
	 */
	AttributeDatatype<AV> getDatatype();

	/**
	 * Create AttributeValue in internal model (suitable for Expression evaluators) from XML/JAXB mixed content and other XML attributes.
	 * <p>
	 * NB: the standard datatype 'xpathExpression' may seem like a special case because xpathExpression evaluation depends on the context; therefore it might seem like a good idea to have
	 * 'xpathExpression' datatype factory - and this interface as a result - use {@link org.ow2.authzforce.core.pdp.api.expression.Expression} as return type of this method instead. However, we prefer
	 * to avoid that for simplicity. Indeed, if we need to evaluate a 'xpathExpression', we don't need a generic interface like {@link org.ow2.authzforce.core.pdp.api.expression.Expression} because in
	 * standard XACML, xpathExpressions are used only as parameters of XPath-based functions (A.3.15), and such functions just need to cast input values to
	 * {@link org.ow2.authzforce.core.pdp.api.value.XPathValue} and call {@link org.ow2.authzforce.core.pdp.api.value.XPathValue#evaluate(org.ow2.authzforce.core.pdp.api.EvaluationContext)} for
	 * evaluation. Outside the context of XPath-based functions, we may consider xpathExpressions as simple literal constants like other AttributeValues.
	 * 
	 * @param content
	 *            raw value's mixed content, e.g. an element might be a {@link String} for text, or {@link org.w3c.dom.Element} for XML element in the case of core XACML (XML) implemented with JAXB
	 *            framework; or possibly {@link Number} or {@link Boolean} in the case of JSON Profile of XACML implemented with common JSON frameworks. This list is a singleton in most cases.
	 * @param otherAttributes
	 *            non-null/mandatory list of other XML attributes of the value node; may be empty if none
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, e.g. XACML xpathExpression
	 * @return attribute value in internal model suitable for Expression evaluators
	 * @throws IllegalArgumentException
	 *             if content/otherAttributes are not valid for the datatype handled by this factory
	 */
	AV getInstance(final List<Serializable> content, Map<QName, String> otherAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException;
}