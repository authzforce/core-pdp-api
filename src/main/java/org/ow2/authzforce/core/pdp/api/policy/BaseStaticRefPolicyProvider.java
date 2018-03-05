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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Convenient base class for {@link CloseableStaticRefPolicyProvider} implementations
 * 
 */
public abstract class BaseStaticRefPolicyProvider implements CloseableStaticRefPolicyProvider
{
	private final int maxPolicySetRefDepth;

	/**
	 * Creates RefPolicyProvider instance
	 * 
	 * @param maxPolicySetRefDepth
	 *            max policy reference (e.g. XACML PolicySetIdReference) depth, i.e. max length of the chain of policy references
	 */
	public BaseStaticRefPolicyProvider(final int maxPolicySetRefDepth)
	{
		this.maxPolicySetRefDepth = maxPolicySetRefDepth < 0 ? UNLIMITED_POLICY_REF_DEPTH : maxPolicySetRefDepth;
	}

	@Override
	public final Deque<String> joinPolicyRefChains(final Deque<String> policyRefChain1, final List<String> policyRefChain2) throws IllegalArgumentException
	{
		return RefPolicyProvider.joinPolicyRefChains(policyRefChain1, policyRefChain2, maxPolicySetRefDepth);
	}

	/**
	 * Resolve reference to Policy, e.g. PolicyIdReference
	 * 
	 * @param policyIdRef
	 *            target PolicyId
	 * @param constraints
	 *            policy version match rules
	 * @return policy evaluator the policy matching the policy reference; or null if no match
	 * @throws IndeterminateEvaluationException
	 *             error resolving policy
	 */
	protected abstract StaticTopLevelPolicyElementEvaluator getPolicy(String policyIdRef, Optional<PolicyVersionPatterns> constraints) throws IndeterminateEvaluationException;

	/**
	 * Finds a policySet based on an reference. This may involve using the reference as indexing data to lookup a policy.
	 * 
	 * @param policyIdRef
	 *            the target PolicySetId
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in java.net.URI.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use String instead.
	 *            </p>
	 *            <p>
	 *            See also:
	 *            </p>
	 *            <p>
	 *            https://java.net/projects/jaxb/lists/users/archive/2011-07/ message/16
	 *            </p>
	 *            <p>
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the java.net.URI constructor.
	 * @param constraints
	 *            any optional constraints on the version of the target policy, matched against its Version attribute
	 * @param policySetRefChainWithPolicyIdRef
	 *            null iff this is not called to resolve a PolicySetIdReference; else this is the chain of PolicySets linked via PolicySetIdReference(s), from the root PolicySet up to (and including)
	 *            {@code policyIdRef}. Each item in the chain is a PolicySetId of a PolicySet that is referenced by the previous item (except the first item which is the root policy) and references
	 *            the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops (circular references) and prevent exceeding reference
	 *            depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * 
	 * @return the policySet matching the policySet reference; or null if no match
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type {@code policyType}
	 */
	protected abstract StaticTopLevelPolicyElementEvaluator getPolicySet(String policyIdRef, Optional<PolicyVersionPatterns> constraints, Deque<String> policySetRefChainWithPolicyIdRef)
			throws IndeterminateEvaluationException;

	@Override
	public final StaticTopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType refPolicyType, final String policyIdRef, final Optional<PolicyVersionPatterns> constraints,
			final Deque<String> policySetRefChain) throws IndeterminateEvaluationException
	{
		if (refPolicyType == TopLevelPolicyElementType.POLICY)
		{
			return getPolicy(policyIdRef, constraints);
		}

		return getPolicySet(policyIdRef, constraints, policySetRefChain);
	}

	@Override
	public final TopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String policyId, final Optional<PolicyVersionPatterns> policyVersionConstraints,
			final Deque<String> policySetRefChain, final EvaluationContext evaluationCtx) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		return get(policyType, policyId, policyVersionConstraints, policySetRefChain);
	}

}