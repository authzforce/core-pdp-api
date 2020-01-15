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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;

import com.google.common.collect.ImmutableList;

/**
 * Result of evaluation of {@link Decidable} (Policy, Rule...) with PEP actions (Obligations/Advice).
 * 
 */
public interface DecisionResult extends ExtendedDecision
{

	/**
	 * Get PEP actions (Obligations/Advices), may be empty - but <b>not null</b> - if the decision is neither Permit or Deny
	 * 
	 * @return PEP actions
	 */
	ImmutableList<PepAction> getPepActions();

	/**
	 * Get the list of the "applicable" policy elements (XACML Policy/PolicySet elements) that contributed to this decision.
	 * <p>
	 * The XACML specification is ambiguous about what is considered an "applicable" policy, especially it does not state clearly which policies should be added to the PolicyIdentifierList in the
	 * final XACML Result. See the discussion here for more info: https://lists.oasis-open.org/archives/xacml-comment/201605/msg00004.html. Here we define an "applicable" policy more explicitly:
	 * <p>
	 * A policy is "applicable" if and only if its evaluation result is different from NotApplicable (not NotApplicable means Applicable, shouldn't it?), and one of these two conditions is met:
	 * <ul>
	 * <li>The policy/policy reference has no enclosing policy, i.e. it is the root policy in PDP's evaluation.</li>
	 * <li>The policy has an enclosing policy and the enclosing policy is "applicable". (This definition is recursive.)</li>
	 * </ul>
	 * More formally: {@code isApplicable(policy) iff evaluate(policy) != NotApplicable && (policy.parent == null || isApplicable(policy.parent)) }
	 * 
	 * @return identifiers of policies found applicable for the decision request. Must be null if and only if the decision is NotApplicable. In particular, if the decision is different from
	 *         NotApplicable but no applicable policy is returned (e.g. it was not requested to return such a list in the request), the returned list must be an empty list, <b>not null</b>.
	 */
	ImmutableList<PrimaryPolicyMetadata> getApplicablePolicies();

}
