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
package org.ow2.authzforce.core.pdp.api.expression;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 *
 * Expression wrapper for primitive constant AttributeValues to be used as Expressions, e.g. as function arguments
 *
 * @param <V>
 *            concrete value type
 * 
 * @version $Id: $
 */
public final class ConstantPrimitiveAttributeValueExpression<V extends AttributeValue> extends ConstantExpression<V>
{

	/**
	 * Creates instance
	 *
	 * @param type
	 *            value datatype
	 * @param v
	 *            static value
	 */
	public ConstantPrimitiveAttributeValueExpression(final Datatype<V> type, final V v)
	{
		super(type, v);
	}

	/** {@inheritDoc} */
	@Override
	public JAXBElement<AttributeValueType> getJAXBElement()
	{
		// create new JAXB AttributeValue as defensive copy (JAXB AttributeValue is not immutable)
		return JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createAttributeValue(this.value);
	}
}
