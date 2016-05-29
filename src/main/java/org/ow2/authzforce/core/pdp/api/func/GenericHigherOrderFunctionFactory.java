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
package org.ow2.authzforce.core.pdp.api.func;

import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.DatatypeFactory;

/**
 * Interface for generic higher-order function factories, such as the one used for the standard map function in Authzforce PDP engine. A generic function is a function class with a type parameter
 * depending on the sub-function's return type, e.g. the standard map function, therefore the function is instantiated for a specific sub-function's return type.
 *
 * 
 * @version $Id: $
 */
public abstract class GenericHigherOrderFunctionFactory implements PdpExtension
{
	/**
	 * Returns instance of the Higher-order function
	 *
	 * @param subFunctionReturnTypeFactory
	 *            sub-function's return datatype factory
	 * @return higher-order function instance
	 */
	public abstract <SUB_RETURN_T extends AttributeValue> HigherOrderBagFunction<?, SUB_RETURN_T> getInstance(DatatypeFactory<SUB_RETURN_T> subFunctionReturnTypeFactory);

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return this.getId();
	}

}
