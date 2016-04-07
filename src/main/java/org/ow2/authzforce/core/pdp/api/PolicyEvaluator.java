/**
 * Copyright (C) 2011-2015 Thales Services SAS.
 *
 * This file is part of AuthZForce.
 *
 * AuthZForce is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * AuthZForce is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AuthZForce. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

/**
 * Policy evaluator interface, "Policy" referring to any XACML Policy* element:
 * Policy(Set), Policy(Set)IdReference.
 * 
 */
public interface PolicyEvaluator extends Decidable
{
	
	/**
	 * "isApplicable()" as defined by Only-one-applicable algorithm (section
	 * C.9), i.e. applicable by virtue of its target, i.e. the target matches
	 * the context. {@link #evaluate(EvaluationContext)} already checks first if
	 * the policy is applicable, therefore you may call isApplicable() only if
	 * you only want to check if the policy is applicable. If you want to
	 * evaluate the policy, call {@link #evaluate(EvaluationContext)} right
	 * away. To be used by Only-one-applicable algorithm in particular.
	 * 
	 * @param context
	 *            evaluation context to match
	 * @return whether it is applicable
	 * @throws IndeterminateEvaluationException
	 *             if Target evaluation in this context is "Indeterminate"
	 */
	boolean isApplicable(EvaluationContext context) throws IndeterminateEvaluationException;

	/**
	 * Same as {@link #evaluate(EvaluationContext)} except Target evaluation may
	 * be skipped. To be used by Only-one-applicable algorithm with
	 * <code>skipTarget</code>=true, after calling
	 * {@link #isApplicable(EvaluationContext)} in particular.
	 * 
	 * @param context
	 *            evaluation context
	 * @param skipTarget
	 *            whether to evaluate the Target. If false, this must be
	 *            equivalent to {@link #evaluate(EvaluationContext)}
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
	 * Get extra metadata of the evaluated policy, which metadata may vary
	 * according to the request context; e.g. for elements such as Policy
	 * references, the actual version ID depends on the request context if the
	 * reference uses version patterns and the policy provider resolves the
	 * matching candidate policy version for each evaluation request.
	 * <p>
	 * Implementations must still
	 * guarantee that the result - once computed in a given request context -
	 * remains constant over the lifetime of this request context. This is
	 * required for consistent evaluation. The result may only change from one
	 * request to the other. For that purpose, implementations may use
	 * {@link EvaluationContext#putOther(String, Object)} to cache the result in
	 * the request context and {@link EvaluationContext#getOther(String)} to
	 * retrieve it later.
	 * 
	 * @param evaluationCtx
	 *            request evaluation context
	 * 
	 * @return extra metadata of the evaluated policy
	 * @throws IndeterminateEvaluationException if the extra policy metadata could not be determined in {@code evaluationCtx}
	 */
	ExtraPolicyMetadata getExtraPolicyMetadata(EvaluationContext evaluationCtx) throws IndeterminateEvaluationException;

}
