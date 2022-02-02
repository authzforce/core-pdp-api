/*
 * Copyright 2012-2022 THALES.
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

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

import java.util.Optional;

/**
 * Common interface to {@link StaticPolicyEvaluator} and {@link TopLevelPolicyElementEvaluator} for evaluators of policies with a fixed version (e.g. not dynamic policy references with version match
 * patterns).
 * 
 */
public interface VersionFixedPolicyEvaluator extends PolicyEvaluator
{
	/**
	 * Get policy version. This is declared in the XACML Policy(Set) itself, therefore does not depend on the evaluation context.
	 * 
	 * @return evaluated policy(Set) Version
	 */
	PolicyVersion getPolicyVersion();

	@Override
	default PolicyVersion getPolicyVersion(final EvaluationContext evaluationCtx, final Optional<EvaluationContext> mdpContext) throws IndeterminateEvaluationException
	{
		return getPolicyVersion();
	}

}
