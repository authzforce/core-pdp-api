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
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Extra metadata of a policy element, i.e. metadata of a
 * Policy(Set)/Policy(Set)IdReference besides the ID. The particularity of all
 * these metadata is that they may not be statically defined, but may depend on
 * the evaluation context. E.g. the actual version of a Policy(Set)IdReference
 * defined by version expressions (Version, EarliestVersion, LatestVersion).
 * 
 */
public interface ExtraPolicyMetadata
{

	/**
	 * Get policy version, e.g. for auditing
	 * 
	 * @param evaluationCtx
	 *            request evaluation context
	 * 
	 * @return evaluated policy(Set) Version
	 */
	PolicyVersion getVersion();

	/**
	 * Get the {@code <Policy>s} referenced - directly or indirectly - from the
	 * policy element (which these are metadata of). This is useful to know all
	 * the policies applicable by a top-level policy evaluator.
	 * 
	 * @return the direct/indirect policy references; empty map if the policy
	 *         does not have any policy reference at all. (Null result is not
	 *         allowed.)
	 *         <p>
	 *         Result must be immutable (can be made so with
	 *         {@link Collections#unmodifiableMap(Map)}).
	 * 
	 */
	Map<String, PolicyVersion> getRefPolicies();

	/**
	 * Get the {@code <PolicySet>s} referenced - directly or indirectly - from
	 * the policy element (which these are metadata of). This is useful to know
	 * all the policies applicable by a top-level policy evaluator.
	 * 
	 * @return the direct/indirect policy references; empty map if the policy
	 *         does not have any policy reference at all. (Null result is not
	 *         allowed.)
	 *         <p>
	 *         Result must be immutable (can be made so with
	 *         {@link Collections#unmodifiableMap(Map)}).
	 * 
	 */
	Map<String, PolicyVersion> getRefPolicySets();

	/**
	 * Get longest chain of Policy reference (via Policy(Set)IdReference)
	 * starting from the policy which these are metadata of, in order to limit
	 * the length of such chain. Note that in the current XACML 3.0 model, it is
	 * safe to ignore Policy elements; since they cannot have references.
	 * However, we consider that Policy and PolicySet types could be merged into
	 * one Policy type on the long-term. That's why we define this method at the
	 * interface level on top of both Policy and PolicySet evaluator classes.
	 * Indeed, this interface represents the common behavior of the two.
	 * 
	 * @return longest policy reference chain in this policy; empty list if the
	 *         policy does not have any policy reference. (Null result is not
	 *         allowed.)
	 *         <p>
	 *         Result must be immutable (can be made so with
	 *         {@link Collections#unmodifiableList(java.util.List)}).
	 */
	List<String> getLongestPolicyRefChain();
}
