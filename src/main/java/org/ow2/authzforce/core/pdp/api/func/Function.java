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

import java.util.List;

import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.value.AtomicValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * XACML function.
 * <p>
 * {@link #getId() Returns the function ID (as PDP extension ID)}
 * 
 * @param <RETURN_T>
 *            return type of this function
 */
public interface Function<RETURN_T extends Value> extends AtomicValue, PdpExtension
{
	/**
	 * Prefix used by AuthZForce non-standard functions (PDP extensions). Third-party (outside AuthZForce project) contributions must use a different prefix to avoid conflicts.
	 */
	String AUTHZFORCE_EXTENSION_PREFIX = PdpExtension.AUTHZFORCE_EXTENSION_PREFIX + "function:";

	/**
	 * The standard namespace where all XACML 1.0 spec-defined functions are defined
	 */
	String XACML_NS_1_0 = "urn:oasis:names:tc:xacml:1.0:function:";

	/**
	 * The standard namespace where all XACML 2.0 spec-defined functions are defined
	 */
	String XACML_NS_2_0 = "urn:oasis:names:tc:xacml:2.0:function:";

	/**
	 * The standard namespace where all XACML 3.0 spec-defined functions are defined
	 */
	String XACML_NS_3_0 = "urn:oasis:names:tc:xacml:3.0:function:";

	/**
	 * Gets the return type of the function
	 * 
	 * @return function return type
	 */
	Datatype<RETURN_T> getReturnType();

	/**
	 * Creates new function call with given arguments (Expressions). Any implementation of this method should first validate inputs according to the function signature/definition.
	 * 
	 * @param argExpressions
	 *            function arguments (expressions)
	 * 
	 * @return Function call handle for calling this function which such inputs (with possible changes from original inputs due to optimizations for instance)
	 * 
	 * @throws IllegalArgumentException
	 *             if inputs are invalid for this function
	 */
	FunctionCall<RETURN_T> newCall(List<Expression<?>> argExpressions) throws IllegalArgumentException;

}
