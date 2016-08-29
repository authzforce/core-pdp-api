/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api;

import javax.xml.bind.JAXBElement;

import com.google.common.collect.ImmutableList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

/**
 * Result of evaluation of {@link Decidable} (Policy, Rule...) with Obligations/Advice elements packaged together in a
 * {@link PepActions} field. This is used as intermediate result by the PDP and therefore it is different from the final
 * Result in the Response by the PDP since, for instance, it does not have the optional list of identifiers of all
 * policies found applicable during the evaluation (see {@link PdpDecisionResult} for more information).
 * 
 */
public interface DecisionResult extends ExtendedDecision
{

	/**
	 * Get PEP actions (Obligations/Advices), may be null if the decision is neither Permit or Deny
	 * 
	 * @return PEP actions
	 */
	ImmutablePepActions getPepActions();

	/**
	 * Get the list of the "applicable" policy elements (XACML Policy/PolicySet elements) that contributed to this
	 * decision.
	 * <p>
	 * The XACML specification is ambiguous about what is considered an "applicable" policy, especially it does not
	 * state clearly which policies should be added to the PolicyIdentifierList in the final XACML Result. See the
	 * discussion here for more info: https://lists.oasis-open.org/archives/xacml-comment/201605/msg00004.html. Here we
	 * define an "applicable" policy more explicitly:
	 * <p>
	 * A policy is "applicable" if and only if its evaluation result is different from NotApplicable (not NotApplicable
	 * means Applicable, shouldn't it?), and one of these two conditions is met:
	 * <ul>
	 * <li>The policy/policy reference has no enclosing policy, i.e. it is the root policy in PDP's evaluation.</li>
	 * <li>The policy has an enclosing policy and the enclosing policy is "applicable". (This definition is
	 * recursive.)</li>
	 * </ul>
	 * More formally:
	 * {@code isApplicable(policy) iff evaluate(policy) != NotApplicable && (policy.parent == null || isApplicable(policy.parent)) }
	 * 
	 * @return identifiers of policies found applicable for the decision request. Must be null if and only if the
	 *         decision is NotApplicable. In particular, if the decision is different from NotApplicable but no
	 *         applicable policy is returned (e.g. it was not requested to return such a list in the request), the
	 *         returned list must be an empty list, not null.
	 */
	ImmutableList<JAXBElement<IdReferenceType>> getApplicablePolicies();

}
