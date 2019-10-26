/**
 * Copyright 2012-2019 THALES.
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

import java.util.Optional;
import java.util.Set;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.DecisionResult;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Policy evaluator interface, "Policy" referring to any XACML Policy* element: Policy(Set), Policy(Set)IdReference.
 * 
 */
public interface PolicyEvaluator extends Decidable
{

	/**
	 * "isApplicable()" as defined by Only-one-applicable algorithm (section C.9), i.e. applicable by virtue of its target, i.e. the target matches the context. {@link #evaluate(EvaluationContext)}
	 * already checks first if the policy Target matches, therefore you may call isApplicable() only if you only want to check if the policy is applicable by virtue of its Target. If you want to
	 * evaluate the policy, call {@link #evaluate(EvaluationContext)} right away. To be used by Only-one-applicable algorithm in particular.
	 * 
	 * @param context
	 *            evaluation context to match
	 * @return whether it is applicable
	 * @throws IndeterminateEvaluationException
	 *             if Target evaluation in this context is "Indeterminate"
	 */
	boolean isApplicableByTarget(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Same as {@link #evaluate(EvaluationContext)} except Target evaluation may be skipped. To be used by Only-one-applicable algorithm with <code>skipTarget</code>=true, after calling
	 * {@link #isApplicableByTarget(EvaluationContext)} in particular.
	 * 
	 * @param context
	 *            evaluation context
	 * @param skipTarget
	 *            whether to evaluate the Target. If false, this must be equivalent to {@link #evaluate(EvaluationContext)}
	 * @return decision result
	 */
	DecisionResult evaluate(EvaluationContext context, boolean skipTarget);

	/**
	 * Get type of evaluated policy element (either XACML Policy or XACML PolicySet)
	 * 
	 * @return evaluated policy element type
	 */
	TopLevelPolicyElementType getPolicyElementType();

	/**
	 * Get policy ID, e.g. for auditing
	 * 
	 * @return evaluated Policy(Set)Id
	 */
	String getPolicyId();

	/**
	 * Get policy version, e.g. for auditing. This may depend on the evaluation context in case of a Policy(Set)IdReference evaluator when using dynamic aka context-dependent {@link PolicyProvider}
	 * that resolve policy references at evaluation time based on the context, especially if the policy reference does not specify the version or use non-literal version match rules (with wildcards).
	 * <p>
	 * Implementations must still guarantee that the result - once computed in a given request context - remains constant over the lifetime of this request context. This is required for consistent
	 * evaluation. The result may only change from one request to the other. For that purpose, implementations may use {@link EvaluationContext#putOther(String, Object)} to cache the result in the
	 * request context and {@link EvaluationContext#getOther(String)} to retrieve it later.
	 * 
	 * @param evaluationCtx
	 *            request evaluation context
	 * @return extra metadata of the evaluated policy
	 * @throws IndeterminateEvaluationException
	 *             if the policy version could not be determined in {@code evaluationCtx}
	 */
	PolicyVersion getPolicyVersion(EvaluationContext evaluationCtx) throws IndeterminateEvaluationException;

	/**
	 * Get metadata about the policies enclosed in the evaluated policy (including itself), i.e. whose actual content is enclosed inside the evaluated policy (as opposed to policy references).
	 * <p>
	 * This allows to detect duplicates, i.e. when the same policy (ID and version) is re-used multiple times in the same enclosing policy.
	 * 
	 * @return the set of enclosed policies, including itself. (May be empty if the policy corresponds to a XACML Policy (no child Policy(Set)s, but never null );
	 */
	Set<PrimaryPolicyMetadata> getEnclosedPolicies();

	/**
	 * Get metadata about the child policy references of the evaluated policy, present iff there is any (e.g. no the case for a XACML Policy element). These metadata may depend on the evaluation
	 * context in case of a Policy(Set)IdReference evaluator when using dynamic aka context-dependent {@link PolicyProvider} that resolve policy references at evaluation time based on the context,
	 * especially if the policy reference does not specify the version or use non-literal version match rules (with wildcards).
	 * <p>
	 * Implementations must still guarantee that the result - once computed in a given request context - remains constant over the lifetime of this request context. This is required for consistent
	 * evaluation. The result may only change from one request to the other. For that purpose, implementations may use {@link EvaluationContext#putOther(String, Object)} to cache the result in the
	 * request context and {@link EvaluationContext#getOther(String)} to retrieve it later.
	 * 
	 * @param evaluationCtx
	 *            request evaluation context
	 * 
	 * @return child policy references metadata of the evaluated policy
	 * @throws IndeterminateEvaluationException
	 *             if the metadata could not be determined in {@code evaluationCtx}
	 */
	Optional<PolicyRefsMetadata> getPolicyRefsMetadata(EvaluationContext evaluationCtx) throws IndeterminateEvaluationException;

}
