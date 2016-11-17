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

import oasis.names.tc.xacml._3_0.core.schema.wd_17.FunctionType;

import org.ow2.authzforce.core.pdp.api.JaxbXACMLUtils;
import org.ow2.authzforce.core.pdp.api.func.Function;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;

/**
 *
 * Expression wrapper for functions, to be used when functions are used as arguments (Expressions) of higher-order functions (any-of, all-of, etc.). This is a simple wrapper since it evaluates to a
 * constant which is the Function (with an ID) itself.
 *
 * 
 * @version $Id: $
 */
@SuppressWarnings("rawtypes")
public final class FunctionExpression extends ConstantExpression<Function>
{

	/**
	 * Creates instance
	 *
	 * @param f
	 *            function
	 */
	public FunctionExpression(final Function<?> f)
	{
		super(StandardDatatypes.FUNCTION_DATATYPE, f);
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public JAXBElement<FunctionType> getJAXBElement()
	{
		// create new JAXB AttributeValue as defensive copy (JAXB AttributeValue is not immutable)
		return JaxbXACMLUtils.XACML_3_0_OBJECT_FACTORY.createFunction(new FunctionType(this.value.getId()));
	}
}
