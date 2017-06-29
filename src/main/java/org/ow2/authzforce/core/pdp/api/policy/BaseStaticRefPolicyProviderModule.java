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
import java.util.List;

/**
 * Convenient base class for {@link StaticRefPolicyProviderModule} implementations
 * 
 */
public abstract class BaseStaticRefPolicyProviderModule extends StaticRefPolicyProviderModule
{
	protected final int maxPolicySetRefDepth;

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param maxPolicySetRefDepth
	 *            max policy reference (e.g. XACML PolicySetIdReference) depth, i.e. max length of the chain of policy references
	 * @throws IllegalArgumentException
	 *             if {@code refPolicyProviderMod} is null
	 */
	public BaseStaticRefPolicyProviderModule(final int maxPolicySetRefDepth) throws IllegalArgumentException
	{
		this.maxPolicySetRefDepth = maxPolicySetRefDepth < 0 ? Helper.UNLIMITED_POLICY_REF_DEPTH : maxPolicySetRefDepth;
	}

	@Override
	public Deque<String> checkJoinedPolicyRefChain(final Deque<String> policyRefChain1, final List<String> policyRefChain2)
	{
		return Helper.checkJoinedPolicyRefChain(policyRefChain1, policyRefChain2, maxPolicySetRefDepth);
	}
}