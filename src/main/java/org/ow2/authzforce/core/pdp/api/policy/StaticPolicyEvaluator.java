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
package org.ow2.authzforce.core.pdp.api.policy;

/**
 * Statically-defined policy evaluator interface, "Policy" referring to any XACML Policy* element:
 * Policy(Set), Policy(Set)IdReference. "Static" means here that the whole policy definition is fixed once and for all, i.e. it does not depend on the evaluation context.
 * 
 */
public interface StaticPolicyEvaluator extends PolicyEvaluator
{

	/**
	 * Get (static/context-independent) extra metadata of the evaluated policy. Always return the same result.
	 * 
	 * @return extra metadata of the evaluated policy.
	 */
	ExtraPolicyMetadata getExtraPolicyMetadata();

}
