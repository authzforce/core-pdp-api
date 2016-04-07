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
package org.ow2.authzforce.core.pdp.api;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Policy-by-reference provider, used by the PDP to get policies referenced by
 * Policy(Set)IdReference in PolicySets.
 */
public interface RefPolicyProvider
{
	/**
	 * Utilities for RefPolicyProvider sub-modules
	 *
	 */
	class Utils
	{
		public static final int UNLIMITED_POLICY_REF_DEPTH = -1;

		/**
		 * Appends a valid chain of policy references to another one. The check
		 * consists to look for circular references or excessive length
		 * resulting from appending the one to the other.
		 * 
		 * @param policyRefChain1
		 *            base chain to be appended
		 * @param policyRefChain2
		 *            non-null chain to append to {@code policyRefChain2}
		 *            (typically a result of PolicySetEvaluator#getLongest(),
		 *            therefore a List of Strings)
		 * @param maxPolicyRefDepth
		 *            max PolicySetIdReference depth, i.e. length of chain of
		 *            PolicySetIdReferences
		 * @return non-null chain of references that is {@code policyRefChain1}
		 *         if {@code policyRefChain1 != null}, else a new
		 *         policy-ref-chain
		 * @throws IllegalArgumentException
		 *             {@code policyRefChain2 == null}, or circular reference
		 *             (same ID in both chains) detected or max length (sum of
		 *             the lengths of the two chains) is greater than
		 *             {@code maxPolicyRefDepth}
		 */
		public static Deque<String> appendAndCheckPolicyRefChain(Deque<String> policyRefChain1, List<String> policyRefChain2, int maxPolicyRefDepth) throws IllegalArgumentException
		{
			if (policyRefChain2 == null)
			{
				return policyRefChain1;
			}

			final Deque<String> resultPolicyRefChain = policyRefChain1 == null ? new ArrayDeque<String>() : policyRefChain1;

			// Validate resulting reference depth
			if (maxPolicyRefDepth != UNLIMITED_POLICY_REF_DEPTH)
			{
				final int resultSize = resultPolicyRefChain.size() + policyRefChain2.size();
				if (resultSize > maxPolicyRefDepth)
				{
					throw new IllegalArgumentException("Depth of Policy Reference (" + resultSize + ") > max allowed (" + maxPolicyRefDepth + ") resulting from chaining these 2 chains of references: " + policyRefChain1 + " -> " + policyRefChain2);
				}
			}

			/*
			 * Check for circular reference (loop). We check only the policy ID
			 * because we consider that a mere reference back to the same ID is
			 * not allowed, no matter what the version is.
			 */
			for (final String nextPolicyId : policyRefChain2)
			{
				if (resultPolicyRefChain.contains(nextPolicyId))
				{
					throw new IllegalArgumentException("Invalid PolicySetIdReference: circular reference (loop) detected: " + resultPolicyRefChain + " -> " + nextPolicyId);
				}

				resultPolicyRefChain.add(nextPolicyId);
			}

			return resultPolicyRefChain;
		}
	}

	/**
	 * Finds a policy based on an id reference. This may involve using the
	 * reference as indexing data to lookup a policy.
	 * 
	 * @param policyId
	 *            the identifier used to resolve the policy by its Policy(Set)Id
	 *            <p>
	 *            WARNING: java.net.URI cannot be used here, because not
	 *            equivalent to XML schema anyURI type. Spaces are allowed in
	 *            XSD anyURI [1], not in java.net.URI.
	 *            </p>
	 *            <p>
	 *            [1] http://www.w3.org/TR/xmlschema-2/#anyURI That's why we use
	 *            String instead.
	 *            </p>
	 *            <p>
	 *            See also:
	 *            </p>
	 *            <p>
	 *            https://java.net/projects/jaxb/lists/users/archive/2011-07/
	 *            message/16
	 *            </p>
	 *            <p>
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by
	 *            default since not all possible values of xs:anyURI can be
	 *            passed to the java.net.URI constructor.
	 * @param policyType
	 *            type of policy element requested (policy or policySet)
	 * @param policyVersionConstraints
	 *            any optional constraints on the version of the referenced
	 *            policy, matched against its Version attribute
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the policy
	 *            using reference {@code idRef}. Therefore this argument does
	 *            not include idRef. This chain is used to control all
	 *            PolicySetIdReferences found within the result policy, i.e.
	 *            detect loops (circular references) and validate reference
	 *            depth.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is
	 *            FIFO, and we need LIFO and iteration in order of insertion, so
	 *            different from Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * @param evaluationCtx
	 *            evaluation context; the policy may be resolved dynamically for
	 *            each evaluation request. Still, the implementation must
	 *            guarantee that the same reference (same {@code refPolicyType},
	 *            {@code policyIdRef}, {@code constraints} arguments) always
	 *            resolves to the same policy in the same evaluation context
	 *            (for the same request) to preserve evaluation consistency.
	 *            Therefore, it is recommended that the implementation caches
	 *            the resolved policy matching given Policy(Set)IdReference
	 *            parameters (policy type, ID, version constraints) in the
	 *            request context {@code evaluationCtx} once and for all using
	 *            {@link EvaluationContext#putOther(String, Object)}, and
	 *            retrieves it in the same context using
	 *            {@link EvaluationContext#getOther(String)} if necessary.
	 * 
	 * @return the policy matching the policy reference; or null if no match
	 * @throws IllegalArgumentException
	 *             The resolved policy is invalid. The policy Provider module
	 *             may parse policies lazily or on the fly, i.e. only when the
	 *             policy is requested/looked for.
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type
	 *             {@code policyType}
	 */
	TopLevelPolicyElementEvaluator get(TopLevelPolicyElementType policyType, String policyId, VersionPatterns policyVersionConstraints, Deque<String> policySetRefChain, EvaluationContext evaluationCtx) throws IllegalArgumentException, IndeterminateEvaluationException;

}