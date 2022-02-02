/*
 * Copyright 2012-2022 THALES.
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

}
