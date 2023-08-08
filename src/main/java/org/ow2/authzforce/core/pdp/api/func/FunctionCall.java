/*
 * Copyright 2012-2023 THALES.
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

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;
import org.ow2.authzforce.core.pdp.api.value.Datatype;
import org.ow2.authzforce.core.pdp.api.value.Value;

import java.util.Optional;

/**
 * Function call. This is the recommended way of calling any {@link BaseFunction}. This is quite similar to XACML Apply except it does not include the Description field; and arguments are optimized
 * specifically for each function by extending this class accordingly, therefore they might be quite different from original input Expressions of the {@literal Apply}. In particular, if some expressions are
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
	 *           Individual Decision evaluation context
	 * @param mdpContext
	 * 	 context of the Multiple Decision request that the {@code context} belongs to if the Multiple Decision Profile is used
	 * @return result of the call
	 * @throws IndeterminateEvaluationException
	 *             if any evaluation error
	 */
	RETURN_T evaluate(EvaluationContext context, Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException;

}
