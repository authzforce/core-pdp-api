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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.Deque;

import org.ow2.authzforce.core.pdp.api.IndeterminateEvaluationException;

/**
 * Static Policy-by-reference provider, used by the PDP to get policies referenced by Policy(Set)IdReference in PolicySets. "Static" means here that, given a
 * Policy(Set)IdReference, the returned Policy(Set) is constant (always the same) and statically defined.
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
	 *            WARNING: java.net.URI cannot be used here, because not equivalent to XML schema anyURI type. Spaces are allowed in XSD anyURI [1], not in
	 *            java.net.URI.
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
	 *            From the JAXB spec: "xs:anyURI is not bound to java.net.URI by default since not all possible values of xs:anyURI can be passed to the
	 *            java.net.URI constructor.
	 * @param refPolicyType
	 *            type of policy element requested (policy or policySet)
	 * @param constraints
	 *            any optional constraints on the version of the referenced policy, matched against its Version attribute
	 * @param policySetRefChain
	 *            chain of ancestor PolicySetIdReferences leading to the policy using reference {@code idRef}. Therefore this argument does not include idRef.
	 *            This chain is used to control all PolicySetIdReferences found within the result policy, i.e. detect loops (circular references) and validate
	 *            reference depth.
	 *            <p>
	 *            (Do not use a Queue for {@code policySetRefChain} as it is FIFO, and we need LIFO and iteration in order of insertion, so different from
	 *            Collections.asLifoQueue(Deque) as well.)
	 *            </p>
	 * 
	 * @return the policy matching the policy reference; or null if no match
	 * @throws IndeterminateEvaluationException
	 *             if error determining a matching policy of type {@code policyType}
	 */
	StaticTopLevelPolicyElementEvaluator get(TopLevelPolicyElementType refPolicyType, String policyIdRef, VersionPatterns constraints, Deque<String> policySetRefChain) throws IndeterminateEvaluationException;

}