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

import net.sf.saxon.s9api.XPathCompiler;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;

/**
 * Registry of AttributeValue Factories supporting multiple datatypes. Any implementation of this must guarantee that there is a one-to-one relationship between AttributeValue (sub)classes and
 * datatype URIs (AttributeValueType DataType field)
 * 
 */
public interface DatatypeFactoryRegistry extends PdpExtensionRegistry<DatatypeFactory<?>>
{

	/**
	 * Create internal model's AttributeValue expression
	 * 
	 * @param value
	 *            AttributeValue from OASIS XACML model
	 * @param xPathCompiler
	 *            XPath compiler for evaluating/compiling any XPath expression in {@code value}
	 * @return expression, e.g. {@link org.ow2.authzforce.core.pdp.api.expression.ConstantExpression} for constant AttributeValues, or something like XPathExpression for context-dependent
	 *         xpathExpression-type of AttributeValues (quite similar to AttributeSelector)
	 * @throws IllegalArgumentException
	 *             value datatype unknown/not supported, or if value cannot be parsed into the value's defined datatype
	 */
	ConstantExpression<? extends AttributeValue> newExpression(AttributeValueType value, XPathCompiler xPathCompiler) throws IllegalArgumentException;
}
