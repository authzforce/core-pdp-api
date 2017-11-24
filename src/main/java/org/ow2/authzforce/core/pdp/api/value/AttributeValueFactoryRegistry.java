/**
 * Copyright 2012-2017 Thales Services SAS.
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

import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;

/**
 * Registry of AttributeValue Factories supporting multiple datatypes. Any implementation of this must guarantee that there is a one-to-one relationship between AttributeValue (sub)classes and
 * datatype URIs (AttributeValueType DataType field)
 * 
 */
public interface AttributeValueFactoryRegistry extends PdpExtensionRegistry<AttributeValueFactory<?>>
{

	/**
	 * Create internal model's AttributeValue expression
	 * 
	 * @param datatypeId
	 *            attribute datatype ID
	 * 
	 * @param content
	 *            raw value's mixed content, e.g. an element might be a {@link String} for text, or {@link org.w3c.dom.Element} for XML element in the case of core XACML (XML) implemented with JAXB
	 *            framework; or possibly {@link Number} or {@link Boolean} in the case of JSON Profile of XACML implemented with common JSON frameworks. This list is a singleton in most cases.
	 * @param otherAttributes
	 *            (mandatory) other XML attributes of the value node, may be empty if none (but not null)
	 * @param xPathCompiler
	 *            XPath compiler for evaluating/compiling any XPath expression in {@code value}
	 * @return expression, e.g. {@link org.ow2.authzforce.core.pdp.api.expression.ConstantExpression} for constant AttributeValues, or something like XPathExpression for context-dependent
	 *         xpathExpression-type of AttributeValues (quite similar to AttributeSelector)
	 * @throws IllegalArgumentException
	 *             value datatype unknown/not supported, or if value cannot be parsed into the value's defined datatype
	 */
	ConstantExpression<? extends AttributeValue> newExpression(String datatypeId, List<Serializable> content, Map<QName, String> otherAttributes, XPathCompiler xPathCompiler)
			throws IllegalArgumentException;
}
