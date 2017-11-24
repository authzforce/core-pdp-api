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
package org.ow2.authzforce.core.pdp.api.expression;

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
		super(StandardDatatypes.FUNCTION, f);
	}

}
