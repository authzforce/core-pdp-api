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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

/**
 * "Decidable" policy element, i.e. policy element that is evaluated to an access control decision: Permit, Deny, etc. As of XACML 3.0, such elements are Rule,
 * Policy and PolicySets, therefore they must implement this interface.
 * 
 */
public interface Decidable
{
	/**
	 * Tries to evaluate the policy by calling the combining algorithm on the given policies or rules.
	 * 
	 * @param context
	 *            the representation of the request
	 * 
	 * @return the result of evaluation
	 */
	DecisionResult evaluate(EvaluationContext context);

}
