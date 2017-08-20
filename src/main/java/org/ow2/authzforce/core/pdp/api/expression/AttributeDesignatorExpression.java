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

import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Bag;

/**
 * AttributeDesignator evaluator
 *
 * @param <AV>
 *            AttributeDesignator evaluation result value's primitive datatype
 * 
 * @version $Id: $
 */
public interface AttributeDesignatorExpression<AV extends AttributeValue> extends Expression<Bag<AV>>
{
	/**
	 * Get Attribute Category/Issuer/Id
	 * 
	 * @return attribute GUID (category, issuer, ID)
	 */
	AttributeFqn getAttributeFQN();

	/**
	 * Indicates whether the attribute's presence is required, i.e. it must have at least one value in this context (non-empty bag). Equivalent of XACML MustBePresent flag.
	 * 
	 * @return true iff the resulting bag must be non-empty, i.e. there must be at least one value for this attribute in the context
	 */
	boolean isNonEmptyBagRequired();

}