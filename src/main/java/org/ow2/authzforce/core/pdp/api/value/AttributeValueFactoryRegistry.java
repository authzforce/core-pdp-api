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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.ow2.authzforce.core.pdp.api.AttributeSource;
import org.ow2.authzforce.core.pdp.api.AttributeSources;
import org.ow2.authzforce.core.pdp.api.PdpExtensionRegistry;
import org.ow2.authzforce.core.pdp.api.expression.ConstantExpression;

import net.sf.saxon.s9api.XPathCompiler;

/**
 * Registry of AttributeValue Factories supporting multiple datatypes. Any implementation of this must guarantee that there is a one-to-one relationship between AttributeValue (sub)classes and
 * datatype URIs (AttributeValueType DataType field).
 * 
 * <p>
 * A valid key in this registry, i.e. valid argument for {@link #getExtension(String)}, is an identifier of a datatype corresponding to the value factory, as returned by
 * {@link AttributeValueFactory#getId()}
 * </p>
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

	/**
	 * Get value factory that can handle a given class of Java values
	 * 
	 * @param rawValueClass
	 * @return compatible attribute value factory; null if none
	 */
	AttributeValueFactory<?> getCompatibleFactory(Class<? extends Serializable> rawValueClass);

	/**
	 * Creates an {@link AttributeValue} from raw value using best compatible {@link AttributeValueFactory} available in this registry; <i>compatible</i> means that it supports
	 * {@code rawValue.getClass()} as input type
	 * 
	 * @param rawValue
	 *            raw value to be converted to {@link AttributeValue}
	 * @return corresponding {@link AttributeValue} in internal model for (XACML) policy evaluation
	 * @throws IllegalArgumentException
	 *             if {@code rawValue} is of the right type but actual value content is not valid for the selected (best compatible available) {@link AttributeValueFactory}. For example, some
	 *             factories for numeric (e.g. integer, double) input may not support all kinds of integer (from -Infinity to +Infinity) but only in a limited range.
	 * @throws UnsupportedOperationException
	 *             if {@code rawValue.getClass()} is not an input type supported by any {@link AttributeValueFactory} in this registry
	 */
	AttributeValue newAttributeValue(final Serializable rawValue) throws IllegalArgumentException, UnsupportedOperationException;

	/**
	 * Creates an {@link AttributeBag} (bag of {@link AttributeValue}s) from collection of raw values using best compatible {@link AttributeValueFactory} available in this registry; <i>compatible</i>
	 * means that it supports the raw value types as input types. Beware that a bag has a single (XACML) datatype, so all value types must correspond to a single (XACML) datatype. Therefore, to
	 * prevent unexpected behavior, {@code rawVals} should all have the same concrete type. In other words, mixing different concrete Java types may cause {@code IllegalArgumentException}. For
	 * example, do not mix Integer (likely to be mapped to XACML integer) and Double (likely to be map to XACML double).
	 * 
	 * @param rawValues
	 *            raw values to be converted to {@link AttributeBag}
	 * @param attributeValueSource
	 *            attribute value source
	 * @return corresponding {@link AttributeBag} in internal model for (XACML) policy evaluation
	 * @throws IllegalArgumentException
	 *             if one of the {@code rawValues} is of the right type but actual value content is not valid for the selected (best compatible available) {@link AttributeValueFactory} (for example,
	 *             some factories for numeric input, e.g. integer or double, may not support all kinds of integer (from -Infinity to +Infinity) but only in a limited range); or {@code rawValues} do
	 *             not have a common type that can be mapped to a common (XACML) AttributeValue datatype, i.e. the input type mixing is not allowed by the factory.
	 * @throws UnsupportedOperationException
	 *             if one of the {@code rawValues} does not have a type supported by any {@link AttributeValueFactory} as input type in this registry
	 */
	AttributeBag<?> newAttributeBag(Collection<? extends Serializable> rawValues, AttributeSource attributeValueSource) throws UnsupportedOperationException, IllegalArgumentException;

	/**
	 * Same as {@link #newAttributeBag(Collection, AttributeSource)} but with {@code attributeValueSource} set to {@link AttributeSources#REQUEST}
	 * 
	 * @param rawValues
	 *            raw values to be converted to {@link AttributeBag}
	 * @return corresponding {@link AttributeBag} in internal model for (XACML) policy evaluation
	 * @throws IllegalArgumentException
	 *             if one of the {@code rawValues} is of the right type but actual value content is not valid for the selected (best compatible available) {@link AttributeValueFactory} (for example,
	 *             some factories for numeric input, e.g. integer or double, may not support all kinds of integer (from -Infinity to +Infinity) but only in a limited range); or {@code rawValues} do
	 *             not have a common type that can be mapped to a common (XACML) AttributeValue datatype, i.e. the input type mixing is not allowed by the factory.
	 * @throws UnsupportedOperationException
	 *             if one of the {@code rawValues} does not have a type supported by any {@link AttributeValueFactory} as input type in this registry
	 */
	AttributeBag<?> newAttributeBag(final Collection<? extends Serializable> rawValues) throws UnsupportedOperationException, IllegalArgumentException;

}
