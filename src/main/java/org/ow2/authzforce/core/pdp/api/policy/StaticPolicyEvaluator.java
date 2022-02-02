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

import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;

/**
 * Statically-defined policy evaluator interface, "policy" referring to any XACML Policy* element: Policy(Set), Policy(Set)IdReference. "Static" means here that the whole policy definition is fixed
 * once and for all at initialization time, i.e. it does not depend on the evaluation context.
 * 
 */
public interface StaticPolicyEvaluator extends VersionFixedPolicyEvaluator
{
	/**
	 * Get (static/context-independent) metadata about policy references within the evaluated policy. Always return the same result.
	 * 
	 * @return metadata about policy references within the evaluated policy. Not present if there is no such reference, e.g. the evaluated policy is a XACML Policy element.
	 */
	Optional<PolicyRefsMetadata> getPolicyRefsMetadata();

	@Override
	default Optional<PolicyRefsMetadata> getPolicyRefsMetadata(final EvaluationContext context, Optional<EvaluationContext> mdpContext)
	{
		return getPolicyRefsMetadata();
	}
}
