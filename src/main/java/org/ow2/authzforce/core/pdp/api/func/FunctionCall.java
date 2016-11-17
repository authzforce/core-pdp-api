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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api.func;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

/**
 * Function call. This is the recommended way of calling any {@link BaseFunction}. This is quite similar to XACML Apply except it does not include the Description field; and arguments are optimized
 * specifically for each function by extending this class accordinly, therefore they might be quite different from original input Expressions of the Apply. In particular, if some expressions are
 * actually static values (e.g. AttributeValue, VariableReference to AttributeValue, function applied to static values...), these expressions might be pre-compiled/pre-evaluated. For instance, a
 * static regex parameter to regexp-match function may be pre-compiled to a regex for re-use.
 * 
 * @param <RETURN_T>
 *            call's return type (typically the same as the internal function's)
 * 
 */
public interface FunctionCall<RETURN_T extends Value>
{

	/**
	 * Get the actual return type of this call (same as the internal function's return type), used as return type for XACML Apply in PDP.
	 * 
	 * @return return type
	 */
	Datatype<RETURN_T> getReturnType();

	/**
	 * Make the call in a given evaluation context
	 * 
	 * @param context
	 *            evaluation context
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if any evaluation error
	 */
	RETURN_T evaluate(EvaluationContext context) throws IndeterminateEvaluationException;

}
