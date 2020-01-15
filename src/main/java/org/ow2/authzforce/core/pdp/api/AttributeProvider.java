/**
 * Copyright 2012-2020 THALES.
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
package org.ow2.authzforce.core.pdp.api;

import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.AttributeValue;
import org.ow2.authzforce.core.pdp.api.value.Datatype;

/**
 * Attribute provider used to resolve {@link oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType}s in a specific way (e.g. from a specific attribute source)
 * 
 */
public interface AttributeProvider
{

	/**
	 * Provides values of the attribute matching the given designator data. If no value found, but no other error occurred, an empty bag is returned.
	 * 
	 * @param attributeFQN
	 *            the global identifier (Category,Issuer,AttributeId) of the attribute to find
	 * @param context
	 *            the request context
	 * @param datatype
	 *            attribute datatype
	 * @return the result of retrieving the attribute, which will be a bag of values of type defined by {@code returnDatatype}; empty bag iff no value found and no error occurred.
	 * @throws UnsupportedOperationException
	 *             {@code attributeFQN} or {@code returnDatatype} are not supported (the PDP engine should try another attribute provider if any)
	 * @throws IndeterminateEvaluationException
	 *             {@code attributeFQN} or {@code returnDatatype} are supported but some error occurred while trying to resolve the attribute value(s)
	 */
	<AV extends AttributeValue> AttributeBag<AV> get(AttributeFqn attributeFQN, Datatype<AV> datatype, EvaluationContext context) throws IndeterminateEvaluationException;

}
