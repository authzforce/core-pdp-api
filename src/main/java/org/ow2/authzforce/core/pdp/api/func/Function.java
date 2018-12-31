/**
 * Copyright 2012-2018 THALES.
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
package org.ow2.authzforce.core.pdp.api.func;

import java.util.List;

import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.expression.Expression;
import org.ow2.authzforce.core.pdp.api.value.PrimitiveValue;
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
public interface Function<RETURN_T extends Value> extends PrimitiveValue, PdpExtension
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
