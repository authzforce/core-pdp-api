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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.Deque;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Static RefPolicyProviderModule, "Static" meaning that given a policy reference (type, ID, version constraints), the returned policy is always the same (no dependency on the evaluation context)
 * 
 */
public abstract class StaticRefPolicyProviderModule implements RefPolicyProviderModule, StaticRefPolicyProvider
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ow2.authzforce.core.pdp.api.policy.RefPolicyProvider#get(org.ow2.authzforce.core.pdp.api.policy.TopLevelPolicyElementType, java.lang.String, java.util.Optional, java.util.Deque,
	 * org.ow2.authzforce.core.pdp.api.EvaluationContext)
	 */
	@Override
	public final TopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String policyId, final Optional<VersionPatterns> policyVersionConstraints,
			final Deque<String> policySetRefChain, final EvaluationContext evaluationCtx) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		return get(policyType, policyId, policyVersionConstraints, policySetRefChain);
	}
}
