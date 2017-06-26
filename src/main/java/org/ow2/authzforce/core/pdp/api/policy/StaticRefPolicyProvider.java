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

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Static Policy-by-reference provider, used by the PDP to get policies referenced by Policy(Set)IdReference in PolicySets. "Static" means here that, given a Policy(Set)IdReference, the returned
 * Policy(Set) is constant (always the same) and statically defined.
 * 
 */
public interface StaticRefPolicyProvider extends RefPolicyProvider
{

	/**
	 * Finds a policy based on an id reference. This may involve using the reference as indexing data to lookup a policy.
	 * 
	 * @param policyIdRef
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
	 * @param refPolicyType
	 *            type of policy element requested (policy or policySet)
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy, matched against its Version attribute
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the policy using reference {@code idRef}. Therefore this argument does not include idRef. This chain is used to control all
	 *            PolicySetIdReferences found within the result policy, i.e. detect loops (circular references) and validate reference depth.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * 
	 * @return the policy matching the policy reference; or null if no match
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type {@code policyType}
	 */
	StaticTopLevelPolicyElementEvaluator get(TopLevelPolicyElementType refPolicyType, String policyIdRef, Optional<VersionPatterns> constraints, Deque<String> policySetRefChain)
			throws IndeterminateEvaluationException;

}