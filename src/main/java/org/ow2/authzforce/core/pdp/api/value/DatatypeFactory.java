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
package org.ow2.authzforce.core.pdp.api.value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.sf.saxon.s9api.XPathCompiler;

import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.w3c.dom.Element;

/**
 * Datatype-specific Attribute Value Factory.
 * 
 * @param <INSTANCE_AV>
 *            type of instance (attribute values) created by this factory
 */
public interface DatatypeFactory<INSTANCE_AV extends AttributeValue> extends PdpExtension
{
	/**
	 * Get datatype of values created by this factory
	 * 
	 * @return supported attribute value datatype
	 */
	Datatype<INSTANCE_AV> getDatatype();

	/**
	 * Gets empty bag
	 * 
	 * @return empty bag
	 */
	Bag<INSTANCE_AV> getEmptyBag();

	/**
	 * Gets empty bag
	 * 
	 * @return empty bag
	 */
	BagDatatype<INSTANCE_AV> getBagDatatype();

	/**
	 * Get class of array of instances of this datatype
	 * 
	 * @return class of array where the component type is this datatype
	 */
	Class<INSTANCE_AV[]> getArrayClass();

	/**
	 * Create attribute value from XML/JAXB mixed content and other XML attributes
	 * 
	 * @param content
	 *            list of (XACML/JAXB) AttributeValueType's mixed content elements of the following types: {@link String}, {@link Element}
	 * @param otherAttributes
	 *            other XML attributes
	 * @param xPathCompiler
	 *            XPath compiler for compiling/evaluating XPath expressions in values, e.g. XACML xpathExpression
	 * @return attribute value in internal model compatible with expression evaluator
	 * @throws IllegalArgumentException
	 *             if content/otherAttributes are not valid for the datatype handled by this factory
	 */
	INSTANCE_AV getInstance(List<Serializable> content, Map<QName, String> otherAttributes, XPathCompiler xPathCompiler) throws IllegalArgumentException;

	/**
	 * returns true iff the expression based on this value always evaluates to the same constant in any evaluation context (not the case of xpathExpressions for instance).
	 * 
	 * @return true iff any value expression created by this factory is static
	 */
	boolean isExpressionStatic();
}