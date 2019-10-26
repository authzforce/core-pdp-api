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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Policy provider, used by the PDP to get policies given a Policy(Set)IdReference (e.g. in PolicySets).
 * 
 * @param <PE>
 *            type of returned PolicyEvaluator
 */
public interface PolicyProvider<PE extends TopLevelPolicyElementEvaluator>
{
	/**
	 * Exception thrown if first arg to {@link #joinPolicyRefChains(Deque, List, int)} is null
	 */
	IllegalArgumentException NULL_POLICYREF_CHAIN1_ARGUMENT_EXCEPTION = new IllegalArgumentException("Null first arg (policyRefChain1)");

	/**
	 * Constant for unlimited depth of policy reference
	 */
	int UNLIMITED_POLICY_REF_DEPTH = -1;

	/**
	 * Join chains of policy references, after checking whether the joined chain does not result in a circular reference (loop) or excessive length.
	 * <p>
	 * {@code policyRefChain1} and {@code policyRefChain2} are chains of PolicySets linked via PolicySetIdReferences. Each item is a PolicySetId of a PolicySet that is referenced by the previous item
	 * (except the first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops
	 * (circular references) and prevent exceeding reference depth.
	 * <p>
	 * Beware that we only keep the IDs in the chain, and not the versions, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 * <p>
	 * (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 * </p>
	 * 
	 * @param policyRefChain1
	 *            mandatory/non-null first part of the joined chain
	 * @param policyRefChain2
	 *            chain (list of policy identifiers) to append to {@code policyRefChain1} (typically a result of {@link PolicyEvaluator#getPolicyRefsMetadata(EvaluationContext)}
	 *            (#getLongestPolicyRefChain) to create the joined chain
	 * @return new joined chain that is {@code policyRefChain1} if {@code policyRefChain2 == null || policyRefChain2.isEmpty()}, else {@code policyRefChain2} appended to {@code policyRefChain1}
	 * @param maxPolicyRefDepth
	 *            max policy reference (e.g. XACML PolicySetIdReference) depth, i.e. max length of the chain of policy references
	 * @throws IllegalArgumentException
	 *             {@code policyRefChain1 == null}, or circular reference (same ID in both chains) detected or resulting length (sum of the lengths of the two chains) is greater than
	 *             {@code maxPolicyRefDepth}
	 */
	static Deque<String> joinPolicyRefChains(final Deque<String> policyRefChain1, final List<String> policyRefChain2, final int maxPolicyRefDepth) throws IllegalArgumentException
	{
		if (policyRefChain1 == null)
		{
			throw NULL_POLICYREF_CHAIN1_ARGUMENT_EXCEPTION;
		}

		if (policyRefChain2 == null || policyRefChain2.isEmpty())
		{
			return policyRefChain1;
		}

		// Validate resulting reference depth
		if (maxPolicyRefDepth != UNLIMITED_POLICY_REF_DEPTH)
		{
			final int resultRefDepth = policyRefChain1.size() + policyRefChain2.size() - 1;
			if (resultRefDepth > maxPolicyRefDepth)
			{
				throw new IllegalArgumentException("Depth of Policy Reference (" + resultRefDepth + ") > max allowed (" + maxPolicyRefDepth + ") resulting from chaining these 2 chains of references: "
				        + policyRefChain1 + " -> " + policyRefChain2);
			}
		}

		final Deque<String> resultPolicyRefChain = new ArrayDeque<>(policyRefChain1);
		/*
		 * Check for circular reference (loop). We check only the policy ID because we consider that a mere reference back to the same ID is not allowed, no matter what the version is.
		 */
		policyRefChain2.stream().forEach(policyId -> {
			if (policyRefChain1.contains(policyId))
			{
				throw new IllegalArgumentException("Invalid PolicySetIdReference: circular reference (loop) detected: " + resultPolicyRefChain + " -> " + policyId);
			}

			resultPolicyRefChain.add(policyId);
		});

		return resultPolicyRefChain;
	}

	/**
	 * Join chains of policy references, after checking whether the joined chain does not result in a circular reference (loop) or excessive length.
	 * <p>
	 * {@code policyRefChain1} and {@code policyRefChain2} are chains of PolicySets linked via PolicySetIdReferences. Each item is a PolicySetId of a PolicySet that is referenced by the previous item
	 * (except the first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops
	 * (circular references) and prevent exceeding reference depth.
	 * <p>
	 * Beware that we only keep the IDs in the chain, and not the versions, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 * <p>
	 * (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 * </p>
	 * 
	 * @param policyRefChain1
	 *            mandatory/non-null first part of the joined chain
	 * @param policyRefChain2
	 *            chain (list of policy identifiers) to append to {@code policyRefChain1} (typically a result of {@link PolicyEvaluator#getPolicyRefsMetadata(EvaluationContext)}
	 *            (#getLongestPolicyRefChain) to create the joined chain
	 * @return new joined chain that is {@code policyRefChain1} if {@code policyRefChain2 == null || policyRefChain2.isEmpty()}, else {@code policyRefChain2} appended to {@code policyRefChain1}
	 * @param maxPolicyRefDepth
	 *            max policy reference (e.g. XACML PolicySetIdReference) depth, i.e. max length of the chain of policy references
	 * @throws IllegalArgumentException
	 *             {@code policyRefChain1 == null}, or circular reference (same ID in both chains) detected or resulting length (sum of the lengths of the two chains) is greater than
	 *             {@code maxPolicyRefDepth}
	 */
	Deque<String> joinPolicyRefChains(final Deque<String> policyRefChain1, final List<String> policyRefChain2) throws IllegalArgumentException;

	/**
	 * Finds a policy based on an ID reference. This may involve using the reference as indexing data to lookup a policy.
	 * 
	 * @param policyId
	 *            the identifier used to resolve the policy by its Policy(Set)Id
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
	 * @param policyType
	 *            type of policy element requested (policy or policySet)
	 * @param policyVersionConstraints
	 *            any optional constraints on the version of the referenced policy, matched against its Version attribute
	 * @param policySetRefChain
	 *            null iff this is not called to resolve a PolicySetIdReference; else ({@code policyType == TopLevelPolicyElementType#POLICY_SET}) this is the chain of PolicySets linked via
	 *            PolicySetIdReference(s), from the root PolicySet up to (and including) {@code policyId}. Each item is a PolicySetId of a PolicySet that is referenced by the previous item (except the
	 *            first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to detect loops
	 *            (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * @param evaluationCtx
	 *            evaluation context; the policy may be resolved dynamically for each evaluation request. Still, the implementation must guarantee that the same reference (same {@code refPolicyType},
	 *            {@code policyIdRef}, {@code constraints} arguments) always resolves to the same policy in the same evaluation context (for the same request) to preserve evaluation consistency.
	 *            Therefore, it is recommended that the implementation caches the resolved policy matching given Policy(Set)IdReference parameters (policy type, ID, version constraints) in the request
	 *            context {@code evaluationCtx} once and for all using {@link EvaluationContext#putOther(String, Object)}, and retrieves it in the same context using
	 *            {@link EvaluationContext#getOther(String)} if necessary.
	 * 
	 * @return the policy matching the policy reference; or null if no match
	 * @throws IllegalArgumentException
	 *             The resolved policy is invalid. The policy Provider module may parse policies lazily or on the fly, i.e. only when the policy is requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type {@code policyType}
	 */
	PE get(TopLevelPolicyElementType policyType, String policyId, Optional<PolicyVersionPatterns> policyVersionConstraints, Deque<String> policySetRefChain, EvaluationContext evaluationCtx)
	        throws IllegalArgumentException, IndeterminateEvaluationException;

}