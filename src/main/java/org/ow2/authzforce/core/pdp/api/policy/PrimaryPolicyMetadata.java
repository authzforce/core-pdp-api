/*
 * Copyright 2012-2021 THALES.
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

import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyIssuer;

/**
 * Primary metadata of a determined Policy or PolicySet (more specifically {@link TopLevelPolicyElementEvaluator}), i.e. the main - most discriminating - metadata declared in the policy document
 * itself. (As opposed to secondary or derived metadata which are usually derived from the content of the document.) May be used as unique reference to such policy element, e.g. as a basis for the
 * XACML PolicyIdentifierList.
 * 
 * 
 */
public interface PrimaryPolicyMetadata
{
	/**
	 * Get policy type (non-null)
	 * 
	 * @return policy type (Policy or PolicySet)
	 */
	TopLevelPolicyElementType getType();

	/**
	 * Get policy ID (non-null)
	 * 
	 * @return evaluated policy Version
	 */
	String getId();

	/**
	 * Get policy version (non-null)
	 * 
	 * @return policy Version
	 */
	PolicyVersion getVersion();

	/**
	 * Get policy issuer
	 * 
	 * @return description
	 */
	Optional<PolicyIssuer> getIssuer();

	/**
	 * Get policy description
	 * 
	 * @return description
	 */
	Optional<String> getDescription();

}
