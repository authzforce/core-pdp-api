/**
 * Copyright 2012-2018 Thales Services SAS.
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
package org.ow2.authzforce.core.pdp.api.combining;

import org.ow2.authzforce.core.pdp.api.Decidable;
import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.ExtendedDecision;
import org.ow2.authzforce.core.pdp.api.PdpExtension;
import org.ow2.authzforce.core.pdp.api.PepAction;
import org.ow2.authzforce.core.pdp.api.UpdatableList;
import org.ow2.authzforce.core.pdp.api.policy.PrimaryPolicyMetadata;

/**
 * Combining algorithm. In combining policies, obligations and advice must be handled correctly. Specifically, no obligation/advice may be included in the <code>Result</code> that doesn't match the
 * permit/deny decision being returned. So, if INDETERMINATE or NOT_APPLICABLE is the returned decision, no obligations/advice may be included in the result. If the decision of the combining algorithm
 * is PERMIT or DENY, then obligations/advice with a matching fulfillOn/AppliesTo effect are also included in the result.
 * 
 * @param <T>
 *            type of combined element (Policy, Rule...)
 */
public interface CombiningAlg<T extends Decidable> extends PdpExtension
{

	/**
	 * Combining algorithm evaluator
	 *
	 */
	interface Evaluator
	{
		/**
		 * Runs the combining algorithm in a specific evaluation context
		 * 
		 * @param context
		 *            the request evaluation context
		 * @param updatablePepActions
		 *            output collection where to add the obligation/advice elements returned by the evaluations of the combined elements, if any
		 * @param updatableApplicablePolicyIdList
		 *            output list where to add policies found "applicable" during evaluation if {@code context.isApplicablePolicyIdListRequested()}. See
		 *            {@link EvaluationContext#isApplicablePolicyIdListRequested()} for a definition of "applicable" in this context. The caller must set this to null iff
		 *            {@code !context.isApplicablePolicyIdListRequested()} (the list of applicable policies is not requested).
		 * 
		 * @return combined result
		 */
		ExtendedDecision evaluate(EvaluationContext context, UpdatableList<PepAction> updatablePepActions, UpdatableList<PrimaryPolicyMetadata> updatableApplicablePolicyIdList);
	}

	/**
	 * Get to know whether this is a policy/policySet or rule-combining algorithm
	 * 
	 * @return the combinedElementType
	 */
	Class<T> getCombinedElementType();

	/**
	 * Creates instance of algorithm. To be implemented by algorithm implementations.
	 * 
	 * @param params
	 *            combining algorithm parameters (in order of declaration) that may be associated with a particular child element
	 * @param combinedElements
	 *            combined child elements (in order of declaration)
	 * 
	 * @return an instance of algorithm evaluator
	 * @throws UnsupportedOperationException
	 *             if this is a legacy algorithm and legacy support is disabled
	 * @throws IllegalArgumentException
	 *             if {@code params} are invalid for this algorithm
	 */
	Evaluator getInstance(Iterable<CombiningAlgParameter<? extends T>> params, Iterable<? extends T> combinedElements) throws UnsupportedOperationException, IllegalArgumentException;
}
