/**
 * Copyright 2012-2021 THALES.
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
 * Marker interface for statically-defined top-level policy element (XACML Policy(Set)) evaluators. In such element, the policy references, if any, must be statically resolved at initialization time.
 * <p>
 * This is the type returned by {@link StaticPolicyProvider}
 */
public interface StaticTopLevelPolicyElementEvaluator extends StaticPolicyEvaluator, TopLevelPolicyElementEvaluator
{
	// Merge of StaticPolicyEvaluator and TopLevelPolicyElementEvaluator

}
