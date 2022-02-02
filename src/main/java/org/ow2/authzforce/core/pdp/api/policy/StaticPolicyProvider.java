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

import java.util.Deque;
import java.util.Optional;

import org.ow2.authzforce.core.pdp.api.EvaluationContext;
import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Static Policy Provider, used by the PDP to get policies referenced by Policy(Set)IdReference (e.g. in PolicySets). "Static" means here that, given a Policy(Set)IdReference, the returned Policy(Set)
 * is constant (always the same) and statically defined, i.e. independent of evaluation context.
 * 
 */
public interface StaticPolicyProvider extends PolicyProvider<StaticTopLevelPolicyElementEvaluator>
{

	/**
	 * Finds a policy based on an ID reference. This may involve using the reference as indexing data to look up a policy.
	 * 
	 * @param policyId
	 *            the requested Policy(Set)Id
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
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the java.net.URI constructor".
	 * @param policyType
	 *            type of requested policy element (Policy or PolicySet)
	 * @param versionConstraints
	 *            any optional constraints on the version of the referenced policy, matched against its Version attribute
	 * @param policySetRefChain
	 *            null iff this is not called to resolve a PolicySetIdReference; else ({@code policyType == TopLevelPolicyElementType#POLICY_SET}) this is the chain of PolicySets linked via
	 *            PolicySetIdReference(s), from the root PolicySet up to (and including) {@code policyId}. Each item in the chain is a PolicySetId of a PolicySet that is referenced by the previous
	 *            item (except the first item which is the root policy) and references the next one. This chain is used to control PolicySetIdReferences found within the result policy, in order to
	 *            detect loops (circular references) and prevent exceeding reference depth.
	 *            <p>
	 *            Beware that we only keep the IDs in the chain, and not the version, because we consider that a reference loop on the same policy ID is not allowed, no matter what the version is.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * 
	 * @return the policy matching the policy reference; or null if no match
	 * @throws IndeterminateEvaluationException error resolving the policy
	 * 
	 */
	StaticTopLevelPolicyElementEvaluator get(TopLevelPolicyElementType policyType, String policyId, Optional<PolicyVersionPatterns> versionConstraints, Deque<String> policySetRefChain)
	        throws IndeterminateEvaluationException;

	@Override
	default StaticTopLevelPolicyElementEvaluator get(final TopLevelPolicyElementType policyType, final String policyId, final Optional<PolicyVersionPatterns> policyVersionConstraints,
	        final Deque<String> policySetRefChain, final EvaluationContext evaluationCtx, final Optional<EvaluationContext> mdpCtx) throws IllegalArgumentException, IndeterminateEvaluationException
	{
		return get(policyType, policyId, policyVersionConstraints, policySetRefChain);
	}

}