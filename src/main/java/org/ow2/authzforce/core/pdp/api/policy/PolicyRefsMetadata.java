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
/**
 * 
 */
package org.ow2.authzforce.core.pdp.api.policy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Metadata meant to be associated to a policy (XACML PolicySet) element, that gives metadata about Policy(Set)IdReferences within this policy. These metadata may depend on the evaluation context,
 * e.g. if the Policy Provider(s) use(s) data from the request context to resolve policy references.
 * 
 */
public interface PolicyRefsMetadata
{

	/**
	 * Get the {@code <Policy>s} referenced - directly or indirectly - from the policy element which these are metadata of. This is useful to know all the policies applicable by a top-level policy
	 * evaluator.
	 * 
	 * @return the direct/indirect policy references; empty if the policy does not have any policy reference at all. (Null result is not allowed.)
	 *         <p>
	 *         Result must be immutable (can be made so with {@link Collections#unmodifiableMap(Map)}).
	 * 
	 */
	Set<PrimaryPolicyMetadata> getRefPolicies();

	/**
	 * Get longest chain of Policy reference (via Policy(Set)IdReference) starting from the policy which these are metadata of, in order to limit the length of such chain. Note that in the current
	 * XACML 3.0 model, it is safe to ignore Policy elements; since they cannot have references. However, we consider that Policy and PolicySet types could be merged into one Policy type on the
	 * long-term. That's why we define this method at the interface level on top of both Policy and PolicySet evaluator classes. Indeed, this interface represents the common behavior of the two.
	 * 
	 * @return longest policy reference chain in this policy; empty list if the policy does not have any policy reference. (Null result is not allowed.)
	 *         <p>
	 *         Result must be immutable (can be made so with {@link Collections#unmodifiableList(java.util.List)}).
	 */
	List<String> getLongestPolicyRefChain();
}
